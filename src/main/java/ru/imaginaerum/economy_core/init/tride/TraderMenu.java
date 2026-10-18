package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.imaginaerum.economy_core.init.economy.EconomyBank;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class TraderMenu extends AbstractContainerMenu {

    private static final double BUYBACK_MULT = 2.25;
    @Nullable
    private final EconomyTrader trader; // на клиенте null
    private MerchantOffers offers;

    /** Серверная сторона. */
    public TraderMenu(int id, Inventory inv, EconomyTrader trader) {
        super(EconomyMenus.TRADER.get(), id);
        this.trader = trader;
        this.offers = trader.getOffers();

    }

    /** Клиентская сторона: список торгов приходит в буфере при открытии. */
    public TraderMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(EconomyMenus.TRADER.get(), id);
        this.trader = null;
        buf.readVarInt(); // id сущности, пока не нужен
        this.offers = MerchantOffers.STREAM_CODEC.decode(buf);
    }

    public static int encode(boolean sell, int qty, int index) {
        return ((sell ? 1000 : 0) + Mth.clamp(qty, 1, 999)) * 1000 + index;
    }
    public MerchantOffers getOffers() {
        return offers;
    }

    public int getUses(int index) {
        return index < offers.size() ? offers.get(index).getUses() : 0;
    }

    /** Клиент: подменяет список торгов на свежий. */
    public void replaceOffers(MerchantOffers fresh) {
        offers.clear();
        offers.addAll(fresh);
    }

    // ---------- Обработка кнопок (сервер) ----------

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (trader == null || id < 0) return false;
        int index = id % 1000;
        int qty = (id / 1000) % 1000;
        boolean sell = id / 1_000_000 > 0;
        if (qty <= 0) return false;
        return sell ? sell(player, index, qty) : buy(player, index, qty);
    }

    private boolean buy(Player player, int index, int qty) {
        int deals = 0;
        while (deals < qty && buyOnce(player, index)) {
            deals++;
        }
        if (deals <= 0) return false;

        player.awardStat(Stats.TRADED_WITH_VILLAGER, deals);
        afterTrade(player);
        return true;
    }

    /** Одна сделка без звука и синхронизации. */
    private boolean buyOnce(Player player, int index) {
        if (trader == null) return false;
        if (index < 0 || index >= offers.size()) return false;
        MerchantOffer offer = offers.get(index);
        if (offer.isOutOfStock()) return false;
        if (!trader.canTrade(index)) return false;

        ItemCost a = offer.getItemCostA();
        Optional<ItemCost> b = offer.getItemCostB();
        int needA = TradePricing.countA(offer);     // без лимита 64
        int needB = b.map(ItemCost::count).orElse(0);

        if (count(player, a) < needA) return false;
        if (b.isPresent() && count(player, b.get()) < needB) return false;

        remove(player, a, needA);
        b.ifPresent(c -> remove(player, c, needB));

        int villagerUses = trader.onOfferTraded(index, player);
        syncUses(offer, villagerUses >= 0 ? villagerUses : offer.getUses() + 1);

        give(player, offer.getResult().copy());
        return true;
    }

    private static int count(Player player, ItemCost cost) {
        if (EconomyBank.isCurrency(cost)) {
            return EconomyBank.total(player, cost.item().value());   // инвентарь + счёт
        }
        int n = 0;
        for (ItemStack s : player.getInventory().items) {
            if (cost.test(s)) n += s.getCount();
        }
        return n;
    }

    private static void remove(Player player, ItemCost cost, int amount) {
        if (EconomyBank.isCurrency(cost)) {
            EconomyBank.spend(player, cost.item().value(), amount);
            return;
        }
        int left = amount;
        for (ItemStack s : player.getInventory().items) {
            if (left <= 0) break;
            if (cost.test(s)) {
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
        }
    }
    /** Приводит счётчик использований копии торга к значению у жителя. */
    private static void syncUses(MerchantOffer offer, int target) {
        offer.resetUses();
        for (int i = 0; i < target; i++) {
            offer.increaseUses();
        }
    }
    private boolean sell(Player player, int slot, int maxBatches) {
        if (trader == null) return false;
        List<ItemStack> inv = player.getInventory().items;
        if (slot < 0 || slot >= inv.size() || inv.get(slot).isEmpty()) return false;

        ItemStack proto = inv.get(slot).copyWithCount(1);
        TraderPrices.Quote q = TraderPrices.quote(proto);
        if (q == null) return false;

        int have = countSame(player, proto);
        int batches = Math.min(have / q.count(), maxBatches);
        if (batches <= 0) return false;

        removeSame(player, proto, batches * q.count());
        trader.addBuyback(proto, batches * q.count());
        EconomyBank.credit(player, q.currency(), batches * q.amount());
        trader.spawnSellXp(batches * q.xp());

        afterTrade(player);
        return true;
    }

    private static int countSame(Player player, ItemStack proto) {
        int n = 0;
        for (ItemStack s : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(s, proto)) n += s.getCount();
        }
        return n;
    }

    private static void removeSame(Player player, ItemStack proto, int amount) {
        for (ItemStack s : player.getInventory().items) {
            if (amount <= 0) break;
            if (ItemStack.isSameItemSameComponents(s, proto)) {
                int take = Math.min(amount, s.getCount());
                s.shrink(take);
                amount -= take;
            }
        }
    }

    private void afterTrade(Player player) {
        if (trader != null) {
            trader.playSound(trader.getNotifyTradeSound(), 1.0F, trader.getVoicePitch());
            trader.refreshNow(player);
            this.offers = trader.getOffers();   // брать актуальный список после пересборки
            if (player instanceof ServerPlayer sp) {
                PacketDistributor.sendToPlayer(sp, new SyncOffersPayload(containerId, offers));
            }
        }
        EconomyBank.syncInventory(player);
    }

    private static void give(Player player, ItemStack stack) {
        EconomyBank.giveOrDrop(player, stack);
    }

    // ---------- Служебное ----------

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return trader == null || (trader.isAlive() && player.distanceToSqr(trader) < 64.0D);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (trader != null) {
            trader.setTradingPlayer(null);
        }
    }
}