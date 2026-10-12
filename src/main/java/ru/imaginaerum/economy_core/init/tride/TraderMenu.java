package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import ru.imaginaerum.economy_core.init.economy.EconomyBank;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TraderMenu extends AbstractContainerMenu {

    /** id кнопки: 0..999 — купить торг №i; 1000+i — продать партию; 2000+i — продать всё. */
    public static final int SELL_ONE = 1_000_000;
    public static final int SELL_ALL = 2_000_000;
    public static final int BUY_ALL = 500_000;
    @Nullable
    private final EconomyTrader trader; // на клиенте null
    private final MerchantOffers offers;
    private final List<DataSlot> uses = new ArrayList<>();

    /** Серверная сторона. */
    public TraderMenu(int id, Inventory inv, EconomyTrader trader) {
        super(EconomyMenus.TRADER.get(), id);
        this.trader = trader;
        this.offers = trader.getOffers();
        initUses();
    }

    /** Клиентская сторона: список торгов приходит в буфере при открытии. */
    public TraderMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(EconomyMenus.TRADER.get(), id);
        this.trader = null;
        buf.readVarInt(); // id сущности, пока не нужен
        this.offers = MerchantOffers.STREAM_CODEC.decode(buf);
        initUses();
    }

    private void initUses() {
        for (MerchantOffer offer : offers) {
            DataSlot slot = DataSlot.standalone();
            slot.set(offer.getUses());
            addDataSlot(slot);
            uses.add(slot);
        }
    }

    public MerchantOffers getOffers() {
        return offers;
    }

    public int getUses(int index) {
        return uses.get(index).get();
    }

    // ---------- Обработка кнопок (сервер) ----------

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (trader == null) return false;
        if (id >= SELL_ALL) return sell(player, id - SELL_ALL, true);
        if (id >= SELL_ONE) return sell(player, id - SELL_ONE, false);
        if (id >= BUY_ALL) return buy(player, id - BUY_ALL, true);
        return buy(player, id, false);
    }

    private boolean buy(Player player, int index, boolean all) {
        int deals = 0;
        // 2000 — страховка от бесконечного цикла
        while (deals < 2000 && buyOnce(player, index)) {
            deals++;
            if (!all) break;
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
        int needA = offer.getCostA().getCount();    // пересчитывается каждую сделку
        int needB = offer.getCostB().getCount();

        if (count(player, a) < needA) return false;
        if (b.isPresent() && count(player, b.get()) < needB) return false;

        remove(player, a, needA);
        b.ifPresent(c -> remove(player, c, needB));

        int villagerUses = trader.onOfferTraded(index, player);
        syncUses(offer, villagerUses >= 0 ? villagerUses : offer.getUses() + 1);
        uses.get(index).set(offer.getUses());

        give(player, offer.getResult().copy());
        return true;
    }

    private static int count(Player player, ItemCost cost) {
        if (EconomyBank.isEmerald(cost)) {
            return EconomyBank.total(player);          // инвентарь + счёт
        }
        int n = 0;
        for (ItemStack s : player.getInventory().items) {
            if (cost.test(s)) n += s.getCount();
        }
        return n;
    }

    private static void remove(Player player, ItemCost cost, int amount) {
        if (EconomyBank.isEmerald(cost)) {
            EconomyBank.spend(player, amount);
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
    private boolean sell(Player player, int slot, boolean all) {
        if (trader == null) return false;
        List<ItemStack> inv = player.getInventory().items;
        if (slot < 0 || slot >= inv.size() || inv.get(slot).isEmpty()) return false;

        ItemStack proto = inv.get(slot).copyWithCount(1);
        TraderPrices.Quote q = TraderPrices.quote(proto);        // цену считаем на сервере заново
        if (q == null) return false;

        int have = countSame(player, proto);
        int batches = Math.min(have / q.count(), all ? Integer.MAX_VALUE : 1);
        if (batches <= 0) return false;

        removeSame(player, proto, batches * q.count());
        EconomyBank.giveCurrency(player, q.currency(), batches * q.amount());
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
        // player.awardStat(Stats.TRADED_WITH_VILLAGER);  <- удалить
        if (trader != null) {
            trader.playSound(SoundEvents.VILLAGER_YES, 1.0F, trader.getVoicePitch());
        }
        EconomyBank.syncInventory(player);
    }

    private static int countItem(Player player, Item item) {
        int n = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(item)) n += s.getCount();
        }
        return n;
    }

    private static void removeItem(Player player, Item item, int amount) {
        for (ItemStack s : player.getInventory().items) {
            if (amount <= 0) break;
            if (s.is(item)) {
                int take = Math.min(amount, s.getCount());
                s.shrink(take);
                amount -= take;
            }
        }
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