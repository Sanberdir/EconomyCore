package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.neoforged.neoforge.attachment.AttachmentType;
import ru.imaginaerum.economy_core.init.items.EcoItems;

public final class EconomyBank {

    /** ВРЕМЕННО: вывод со счёта (Shift + ПКМ пустой рукой). Включить: true. */
    public static final boolean WITHDRAW_ENABLED = false;

    // ---------- Валюты ----------

    public static boolean isCurrency(Item item) {
        return item == Items.EMERALD || item == EcoItems.GOLD_COIN.get();
    }

    public static boolean isCurrency(ItemStack s) {
        return isCurrency(s.getItem());
    }

    public static boolean isCurrency(ItemCost cost) {
        return isCurrency(cost.item().value());
    }

    private static AttachmentType<Integer> type(Item item) {
        return item == Items.EMERALD
                ? EconomyAttachments.BALANCE.get()
                : EconomyAttachments.COIN_BALANCE.get();
    }

    // ---------- Счёт ----------

    public static int balance(Player p, Item item) {
        return p.getData(type(item));
    }

    private static void setBalance(Player p, Item item, long v) {
        p.setData(type(item), (int) Math.max(0, Math.min(v, Integer.MAX_VALUE)));
    }

    public static int inInventory(Player p, Item item) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) {
            if (s.is(item)) n += s.getCount();
        }
        return n;
    }

    /** Всё: инвентарь + счёт. */
    public static int total(Player p, Item item) {
        long t = (long) inInventory(p, item) + balance(p, item);
        return (int) Math.min(t, Integer.MAX_VALUE);
    }

    /** Списывает: сначала из инвентаря, остаток со счёта. */
    public static void spend(Player p, Item item, int amount) {
        int left = amount;
        for (ItemStack s : p.getInventory().items) {
            if (left <= 0) break;
            if (s.is(item)) {
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
        }
        if (left > 0) setBalance(p, item, (long) balance(p, item) - left);
    }

    /** Кладёт на счёт всю стопку (изумруды или монеты). */
    public static void deposit(Player p, ItemStack stack) {
        Item item = stack.getItem();
        if (!isCurrency(item)) return;
        int space = Integer.MAX_VALUE - balance(p, item);
        int amount = Math.min(stack.getCount(), space);
        if (amount <= 0) return;
        stack.shrink(amount);
        setBalance(p, item, (long) balance(p, item) + amount);
        feedback(p, Component.translatable("message.economy_core.deposit", amount, item.getDescription()));
    }

    // ---------- Вывод (временно отключён) ----------

    /** Снимает до 64 штук: сначала изумруды, если их нет, то монеты. */
    public static void withdraw(Player p) {
        if (!WITHDRAW_ENABLED) return;
        Item item = balance(p, Items.EMERALD) > 0 ? Items.EMERALD : EcoItems.GOLD_COIN.get();

        int n = Math.min(balance(p, item), 64);
        if (n <= 0) {
            feedback(p, Component.translatable("message.economy_core.empty"));
            return;
        }
        n = Math.min(n, space(p, item));
        if (n <= 0) {
            feedback(p, Component.translatable("message.economy_core.no_room"));
            return;
        }
        setBalance(p, item, (long) balance(p, item) - n);
        addToInventory(p, item, n);
        syncInventory(p);
        feedback(p, Component.translatable("message.economy_core.withdraw", n, item.getDescription()));
    }

    /** Для пакета с клиента: Shift + пустые руки. */
    public static void tryWithdraw(ServerPlayer p) {
        if (!WITHDRAW_ENABLED) return;
        if (p.isShiftKeyDown() && p.getMainHandItem().isEmpty() && p.getOffhandItem().isEmpty()) {
            withdraw(p);
        }
    }

    // ---------- Выдача ----------

    /** Сколько штук влезет в основной инвентарь. */
    public static int space(Player p, Item item) {
        int max = new ItemStack(item).getMaxStackSize();
        int space = 0;
        for (ItemStack s : p.getInventory().items) {
            if (s.isEmpty()) {
                space += max;
            } else if (s.is(item)) {
                space += Math.max(0, s.getMaxStackSize() - s.getCount());
            }
        }
        return space;
    }

    private static void addToInventory(Player p, Item item, int amount) {
        int max = new ItemStack(item).getMaxStackSize();
        int left = amount;
        while (left > 0) {
            int n = Math.min(max, left);
            p.getInventory().add(new ItemStack(item, n));
            left -= n;
        }
    }

    /** Выдаёт валюту: сколько влезет в инвентарь, остаток на счёт. Другие предметы: в инвентарь, остаток на землю. */
    public static void giveCurrency(Player p, Item item, int amount) {
        if (amount <= 0) return;
        if (isCurrency(item)) {
            int fit = Math.min(amount, space(p, item));
            addToInventory(p, item, fit);
            int rest = amount - fit;
            if (rest > 0) {
                setBalance(p, item, (long) balance(p, item) + rest);
                feedback(p, Component.translatable("message.economy_core.to_account", rest, item.getDescription()));
            }
            syncInventory(p);
            return;
        }
        int max = new ItemStack(item).getMaxStackSize();
        int left = amount;
        while (left > 0) {
            int n = Math.min(max, left);
            giveOrDrop(p, new ItemStack(item, n));
            left -= n;
        }
    }

    /** Кладёт в инвентарь; что не влезло, выбрасывает перед игроком. */
    public static void giveOrDrop(Player p, ItemStack stack) {
        if (stack.isEmpty()) return;
        p.getInventory().add(stack);
        if (!stack.isEmpty()) {
            ItemEntity drop = p.drop(stack.copy(), false, true);
            if (drop != null) drop.setPickUpDelay(10);
            stack.setCount(0);
        }
        syncInventory(p);
    }

    /** Полная пересылка инвентаря клиенту. */
    public static void syncInventory(Player p) {
        if (p instanceof ServerPlayer) {
            p.inventoryMenu.sendAllDataToRemote();
        }
    }

    private static void feedback(Player p, Component msg) {
        p.displayClientMessage(msg, true);
        p.level().playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    private EconomyBank() {}
}