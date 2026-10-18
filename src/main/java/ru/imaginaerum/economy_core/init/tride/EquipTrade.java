package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.economy.EconomyBank;

import javax.annotation.Nullable;
import java.util.*;

/** Продажа надетого (броня, левая рука, Curios) и выкуп обратно, пока открыто окно торговли. */
@EventBusSubscriber(modid = EconomyCore.MODID)
public final class EquipTrade {

    private record Sold(int id, String key, ItemStack stack, Item currency, int amount) {}
    private record Price(Item currency, int amount) {}

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.OFFHAND};

    private static final Map<UUID, List<Sold>> SOLD = new HashMap<>();
    private static int nextId = 1;

    // ---------- события ----------
    @SubscribeEvent
    public static void onClose(PlayerContainerEvent.Close e) {
        if (e.getContainer() instanceof TraderMenu m && e.getEntity() instanceof ServerPlayer sp) {
            commit(sp, m);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.containerMenu instanceof TraderMenu m) {
            commit(sp, m);
        }
        SOLD.remove(e.getEntity().getUUID());
    }
    public static void handle(EquipActionPayload p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp) || !(sp.containerMenu instanceof TraderMenu menu)) {
            System.out.println("[EQ] handle rejected, action=" + p.action()
                    + " player=" + ctx.player()
                    + " menu=" + ctx.player().containerMenu);
            return;
        }
        System.out.println("[EQ] handle action=" + p.action() + " menu=" + menu);

        switch (p.action()) {
            case EquipActionPayload.SELL -> sell(sp, p.key());
            case EquipActionPayload.BUYBACK -> buyback(sp, p.id());
            case EquipActionPayload.COMMIT -> { commit(sp, menu); return; }
            default -> { }
        }
        PacketDistributor.sendToPlayer(sp, new EquipListPayload(lines(sp)));
    }

    private static void commit(ServerPlayer sp, TraderMenu menu) {
        List<Sold> list = SOLD.remove(sp.getUUID());
        System.out.println("[EQ] commit, sold=" + (list == null ? "null" : list.size()));
        if (list == null || list.isEmpty()) return;
        List<ItemStack> stacks = new ArrayList<>();
        for (Sold s : list) stacks.add(s.stack());
        menu.commitSold(sp, stacks);
    }



    // ---------- слоты ----------

    private static boolean curios() {
        return ModList.get().isLoaded("curios");
    }

    @Nullable
    private static EquipmentSlot slot(String name) {
        for (EquipmentSlot s : SLOTS) if (s.getName().equals(name)) return s;
        return null;
    }

    /** Ключи: "e:head", "e:offhand", "c:ring:0". */
    private static ItemStack get(ServerPlayer sp, String key) {
        if (key.startsWith("e:")) {
            EquipmentSlot s = slot(key.substring(2));
            return s == null ? ItemStack.EMPTY : sp.getItemBySlot(s);
        }
        if (key.startsWith("c:") && curios()) return CuriosCompat.get(sp, key.substring(2));
        return ItemStack.EMPTY;
    }

    private static void set(ServerPlayer sp, String key, ItemStack stack) {
        if (key.startsWith("e:")) {
            EquipmentSlot s = slot(key.substring(2));
            if (s != null) sp.setItemSlot(s, stack);
        } else if (key.startsWith("c:") && curios()) {
            CuriosCompat.set(sp, key.substring(2), stack);
        }
    }

    private static boolean locked(ServerPlayer sp, String key, ItemStack st) {
        if (!key.startsWith("e:") || key.equals("e:offhand")) return false;
        return !sp.isCreative() && EnchantmentHelper.has(st, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }

    @Nullable
    private static Price price(ItemStack st) {
        TraderPrices.Quote q = TraderPrices.quote(st);
        if (q == null || st.getCount() < q.count()) return null;
        long total = (long) q.amount() * (st.getCount() / q.count());
        return new Price(q.currency(), (int) Math.min(total, Integer.MAX_VALUE));
    }

    private static String id(Item i) {
        return BuiltInRegistries.ITEM.getKey(i).toString();
    }

    // ---------- действия ----------

    public static List<EquipLine> lines(ServerPlayer sp) {
        List<Sold> sold = SOLD.getOrDefault(sp.getUUID(), List.of());

        TreeSet<String> keys = new TreeSet<>(EquipTrade::compareKeys);
        for (EquipmentSlot s : SLOTS) keys.add("e:" + s.getName());
        if (curios()) for (String k : CuriosCompat.keys(sp)) keys.add("c:" + k);
        for (Sold s : sold) keys.add(s.key());   // ключ проданного остаётся на своём месте

        List<EquipLine> out = new ArrayList<>();
        for (String k : keys) {
            for (Sold s : sold) {
                if (s.key().equals(k)) {
                    out.add(new EquipLine(true, s.id(), k, s.stack().copy(), id(s.currency()), s.amount()));
                }
            }
            ItemStack st = get(sp, k);
            if (st.isEmpty()) continue;
            Price pr = locked(sp, k, st) ? null : price(st);
            out.add(new EquipLine(false, 0, k, st.copy(),
                    pr == null ? "" : id(pr.currency()), pr == null ? 0 : pr.amount()));
        }
        return out;
    }

    private static int rank(String k) {
        if (k.startsWith("e:")) {
            String n = k.substring(2);
            for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i].getName().equals(n)) return i;
            return 99;
        }
        return 100;
    }

    private static int idx(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return -1; }
    }

    private static int compareKeys(String a, String b) {
        int ra = rank(a), rb = rank(b);
        if (ra != rb) return Integer.compare(ra, rb);
        if (ra < 100) return 0;
        int ca = a.lastIndexOf(':'), cb = b.lastIndexOf(':');
        int c = a.substring(0, ca).compareTo(b.substring(0, cb));
        return c != 0 ? c : Integer.compare(idx(a.substring(ca + 1)), idx(b.substring(cb + 1)));
    }

    private static void sell(ServerPlayer sp, String key) {
        if (!(sp.containerMenu instanceof TraderMenu menu)) return;
        ItemStack st = get(sp, key);
        if (st.isEmpty() || locked(sp, key, st)) return;

        TraderPrices.Quote q = TraderPrices.quote(st);
        if (q == null || st.getCount() < q.count()) return;

        int batches = st.getCount() / q.count();
        int items = batches * q.count();
        int rest = st.getCount() - items;
        ItemStack sold = st.copyWithCount(items);
        int amount = batches * q.amount();

        set(sp, key, rest > 0 ? st.copyWithCount(rest) : ItemStack.EMPTY);
        if (menu.sellEquipped(sp, sold, q)) {
            SOLD.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>())
                    .add(new Sold(nextId++, key, sold.copy(), q.currency(), amount));
        } else {
            set(sp, key, st);   // не получилось: вернуть
        }
    }

    private static void buyback(ServerPlayer sp, int id) {
        List<Sold> list = SOLD.get(sp.getUUID());
        if (list == null) return;
        Sold s = null;
        for (Sold x : list) if (x.id() == id) { s = x; break; }
        if (s == null) return;

        int have = EconomyBank.isCurrency(s.currency())
                ? EconomyBank.total(sp, s.currency())
                : EconomyBank.inInventory(sp, s.currency());
        if (have < s.amount()) {
            sp.displayClientMessage(Component.translatable("gui.economy_core.trader.equip.no_money"), true);
            return;
        }
        EconomyBank.spend(sp, s.currency(), s.amount());
        list.remove(s);

        ItemStack back = s.stack().copy();
        if (get(sp, s.key()).isEmpty()) {
            set(sp, s.key(), back);              // в тот же слот
        } else {
            EconomyBank.giveOrDrop(sp, back);    // слот занят: в инвентарь
        }
        EconomyBank.syncInventory(sp);
    }

    private EquipTrade() {}
}