package ru.imaginaerum.economy_core.init.entities.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import ru.imaginaerum.economy_core.init.tride.TraderMenu;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EconomyTrader extends AbstractVillager {

    private static final int BED_SEARCH_RADIUS = 200;   // 5 чанков
    private static final int VILLAGE_RADIUS = 200;
    private static final int WANDER_RADIUS = 32;
    private static final int REFRESH_INTERVAL = 200; // тиков
    private record Source(java.util.UUID villager, int index) {}

    private final List<Source> sources = new ArrayList<>();
    @Nullable
    private BlockPos homePos;
    private long lastRestockDay = -1L;

    public EconomyTrader(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }
    @Nullable
    private VillagerProfession specialization;   // null + chosen = универсальный
    private boolean specializationChosen = false;

    @Nullable
    public VillagerProfession getSpecialization() {
        return specialization;
    }

    public boolean isUniversal() {
        return specializationChosen && specialization == null;
    }

    /** Выбирает специализацию из профессий, которые есть в деревне, либо «универсальный». */
    private void chooseSpecialization(List<Villager> all) {
        if (all.isEmpty()) return;   // некого выбирать, решим при следующем обновлении

        if (specializationChosen) {
            if (specialization == null) return;   // универсальный остаётся
            boolean stillPresent = false;
            for (Villager v : all) {
                if (v.getVillagerData().getProfession() == specialization) {
                    stillPresent = true;
                    break;
                }
            }
            if (stillPresent) return;
        }

        List<VillagerProfession> pool = new ArrayList<>();
        for (Villager v : all) {
            VillagerProfession p = v.getVillagerData().getProfession();
            if (!pool.contains(p)) pool.add(p);
        }
        pool.add(null);   // null = универсальный

        this.specialization = pool.get(this.random.nextInt(pool.size()));
        this.specializationChosen = true;
    }
    public static AttributeSupplier.Builder createTraderAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }
    /** "" — ещё не выбрана, "universal" — универсальный, иначе id профессии. */
    public String getSpecializationKey() {
        if (!specializationChosen) return "";
        if (specialization == null) return "universal";
        return BuiltInRegistries.VILLAGER_PROFESSION.getKey(specialization).toString();
    }

    /** Выдаёт случайную профессию, если её ещё нет. */
    private void ensureSpecialization() {
        if (this.specializationChosen || this.specialization != null) return;
        List<VillagerProfession> pool = BuiltInRegistries.VILLAGER_PROFESSION.stream()
                .filter(p -> p != VillagerProfession.NONE && p != VillagerProfession.NITWIT)
                .toList();
        if (!pool.isEmpty()) {
            this.specialization = pool.get(this.random.nextInt(pool.size()));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType type, @Nullable SpawnGroupData data) {
        ensureSpecialization();
        return super.finalizeSpawn(level, difficulty, type, data);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(2, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.35D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // ---------- Взаимодействие ----------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        System.out.println("[Trader] mobInteract called, hand=" + hand
                + ", client=" + this.level().isClientSide);

        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.VILLAGER_SPAWN_EGG) || !this.isAlive() || this.isBaby()) {
            System.out.println("[Trader] skipped: egg/dead/baby -> super.mobInteract");
            return super.mobInteract(player, hand);
        }

        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (this.isTrading()) {
                System.out.println("[Trader] already trading with " + this.getTradingPlayer());
                return InteractionResult.CONSUME;
            }
            try {
                refreshOffers(serverLevel, player);
                System.out.println("[Trader] offers after refresh: " + this.getOffers().size());

                this.setTradingPlayer(player);
                var opened = player.openMenu(
                        new SimpleMenuProvider((id, inv, p) -> new TraderMenu(id, inv, this), this.getDisplayName()),
                        buf -> {
                            buf.writeVarInt(this.getId());
                            MerchantOffers.STREAM_CODEC.encode(buf, this.getOffers());
                            buf.writeUtf(this.getSpecializationKey());
                        });
                System.out.println("[Trader] menu opened: " + opened.isPresent());

                if (opened.isEmpty()) {
                    this.setTradingPlayer(null);
                }
            } catch (Throwable e) {
                System.out.println("[Trader] FAILED to open menu: " + e);
                e.printStackTrace();
                this.setTradingPlayer(null);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        Player tp = this.getTradingPlayer();
        if (tp != null && !(tp.containerMenu instanceof TraderMenu)) {
            this.setTradingPlayer(null);
        }

        if (this.tickCount % REFRESH_INTERVAL == 0
                && this.getTradingPlayer() == null
                && this.level() instanceof ServerLevel serverLevel) {
            refreshOffers(serverLevel, null);
        }
    }

    // ---------- Сбор торгов деревни ----------

    private void refreshOffers(ServerLevel level, @Nullable Player player) {
        ensureSpecialization();
        // 1. Кровать = центр деревни
        if (this.homePos == null || !level.getBlockState(this.homePos).is(BlockTags.BEDS)) {
            this.homePos = level.getPoiManager()
                    .findClosest(holder -> holder.is(PoiTypes.HOME), this.blockPosition(),
                            BED_SEARCH_RADIUS, PoiManager.Occupancy.ANY)
                    .orElse(null);
            if (this.homePos != null) {
                this.restrictTo(this.homePos, WANDER_RADIUS);
            } else {
                this.clearRestriction();
            }
        }

        MerchantOffers offers = this.getOffers();
        offers.clear();
        sources.clear();

        BlockPos center = this.homePos != null ? this.homePos : this.blockPosition();
        AABB box = new AABB(center).inflate(VILLAGE_RADIUS);
        List<Villager> all = level.getEntitiesOfClass(Villager.class, box, v ->
                v.isAlive() && !v.isBaby()
                        && v.getVillagerData().getProfession() != VillagerProfession.NONE
                        && v.getVillagerData().getProfession() != VillagerProfession.NITWIT);

        chooseSpecialization(all);

        List<Villager> villagers = isUniversal()
                ? all
                : all.stream()
                .filter(v -> v.getVillagerData().getProfession() == this.specialization)
                .toList();

        for (Villager villager : villagers) {
            MerchantOffers vo = villager.getOffers(); // сам сгенерирует торги, если их нет
            for (int i = 0; i < vo.size(); i++) {
                MerchantOffer offer = vo.get(i);
                if (!acceptOffer(offer)) continue;

                MerchantOffer copy = new MerchantOffer(
                        offer.getItemCostA(),
                        offer.getItemCostB(),
                        offer.getResult().copy(),
                        offer.getUses(), offer.getMaxUses(),
                        offer.getXp(), offer.getPriceMultiplier(),
                        offer.getDemand());                       // спрос теперь копируется
                if (player != null) {
                    applyPrices(copy, villager, player);
                }

                int dup = indexOfSame(offers, offer);
                if (dup < 0) {
                    offers.add(copy);
                    sources.add(new Source(villager.getUUID(), i));
                } else {
                    MerchantOffer prev = offers.get(dup);
                    boolean better = (prev.isOutOfStock() && !copy.isOutOfStock())
                            || (!copy.isOutOfStock()
                            && copy.getCostA().getCount() < prev.getCostA().getCount());
                    if (better) {                                  // берём где есть запас и дешевле
                        offers.set(dup, copy);
                        sources.set(dup, new Source(villager.getUUID(), i));
                    }
                }
            }
        }
        System.out.println("[Trader] bed=" + this.homePos + ", villagers=" + villagers.size()
                + ", offers=" + offers.size());
    }
    private static void applyPrices(MerchantOffer offer, Villager villager, Player player) {
        offer.resetSpecialPriceDiff();

        int rep = villager.getPlayerReputation(player);
        if (rep != 0) {
            offer.addToSpecialPriceDiff(-Mth.floor(rep * offer.getPriceMultiplier()));
        }

        MobEffectInstance hero = player.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
        if (hero != null) {
            double d = 0.3D + 0.0625D * hero.getAmplifier();
            int discount = (int) Math.floor(d * offer.getBaseCostA().getCount());
            offer.addToSpecialPriceDiff(-Math.max(discount, 1));
        }
    }
    private static int indexOfSame(List<MerchantOffer> list, MerchantOffer offer) {
        for (int i = 0; i < list.size(); i++) {
            if (same(list.get(i), offer)) return i;
        }
        return -1;
    }
    /** Житель-владелец торга №index, если он жив и торг на месте. */
    @Nullable
    private MerchantOffer findSourceOffer(int index, @Nullable Villager[] holder) {
        if (index < 0 || index >= sources.size()) return null;
        if (!(this.level() instanceof ServerLevel sl)) return null;
        Source s = sources.get(index);
        if (!(sl.getEntity(s.villager()) instanceof Villager v) || !v.isAlive()) return null;
        MerchantOffers vo = v.getOffers();
        if (s.index() >= vo.size()) return null;
        if (holder != null) holder[0] = v;
        return vo.get(s.index());
    }

    /** Можно ли сейчас совершить сделку: житель жив и у него есть запас. */
    public boolean canTrade(int index) {
        MerchantOffer orig = findSourceOffer(index, null);
        return orig != null && !orig.isOutOfStock();
    }

    /** Вызывается после оплаты: прокачивает исходного жителя. Возвращает его актуальное число использований. */
    /** Вызывается после оплаты: прокачивает исходного жителя. Возвращает его актуальное число использований. */
    public int onOfferTraded(int index, Player player) {
        Villager[] holder = new Villager[1];
        MerchantOffer orig = findSourceOffer(index, holder);
        if (orig == null) return -1;

        Villager v = holder[0];
        boolean temp = v.getTradingPlayer() == null;
        if (temp) v.setTradingPlayer(player);

        v.notifyTrade(orig);                       // опыт жителя и уровень растут как раньше

        if (temp) v.setTradingPlayer(null);

        // Сферы опыта, которые житель только что создал, переносим на торговца
        if (this.level() instanceof ServerLevel sl && orig.shouldRewardExp()) {
            int xp = 0;
            for (ExperienceOrb orb : sl.getEntitiesOfClass(ExperienceOrb.class,
                    v.getBoundingBox().inflate(1.0D, 1.5D, 1.0D),
                    o -> o.tickCount == 0)) {      // только что созданные
                xp += orb.getValue();
                orb.discard();
            }
            if (xp > 0) {
                ExperienceOrb.award(sl, this.position().add(0, 0.5D, 0), xp);
            }
        }
        return orig.getUses();
    }
    /** Сферы опыта у торговца. */
    public void spawnSellXp(int xp) {
        if (xp > 0 && this.level() instanceof ServerLevel sl) {
            ExperienceOrb.award(sl, this.position().add(0, 0.5D, 0), xp);
        }
    }
    /**
     * Фильтр торгов. Сейчас берём всё.
     * Потом сюда можно добавить разделение на «купить» и «продать».
     */
    protected boolean acceptOffer(MerchantOffer offer) {
        return true;
    }

    private static boolean same(MerchantOffer a, MerchantOffer b) {
        return ItemStack.matches(a.getBaseCostA(), b.getBaseCostA())
                && ItemStack.matches(a.getCostB(), b.getCostB())
                && ItemStack.matches(a.getResult(), b.getResult());
    }

    private static boolean containsSame(List<MerchantOffer> list, MerchantOffer offer) {
        for (MerchantOffer o : list) {
            if (same(o, offer)) return true;
        }
        return false;
    }

    private static int findOldUses(List<MerchantOffer> old, MerchantOffer offer) {
        for (MerchantOffer o : old) {
            if (same(o, offer)) return o.getUses();
        }
        return 0;
    }

    // ---------- Обязательные методы AbstractVillager ----------

    @Override
    protected void updateTrades() {
        // Торги собираются из деревни в refreshOffers()
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        // Опыта у торгаша нет
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    // ---------- NBT ----------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SpecChosen", this.specializationChosen);
        if (this.specialization != null) {
            tag.putString("Specialization",
                    BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.specialization).toString());
            System.out.println("[Trader] specialization chosen: "
                    + (specialization == null ? "UNIVERSAL" : specialization));
        }
        if (this.homePos != null) {
            tag.putIntArray("Home", new int[]{homePos.getX(), homePos.getY(), homePos.getZ()});
        }
        tag.putLong("LastRestockDay", this.lastRestockDay);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int[] home = tag.getIntArray("Home");
        this.specializationChosen = tag.getBoolean("SpecChosen");
        if (tag.contains("Specialization")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("Specialization"));
            if (id != null) {
                BuiltInRegistries.VILLAGER_PROFESSION.getOptional(id)
                        .ifPresent(p -> this.specialization = p);
            }
        }
        if (home.length == 3) {
            this.homePos = new BlockPos(home[0], home[1], home[2]);
            this.restrictTo(this.homePos, WANDER_RADIUS);
        }
        this.lastRestockDay = tag.contains("LastRestockDay") ? tag.getLong("LastRestockDay") : -1L;
    }
}