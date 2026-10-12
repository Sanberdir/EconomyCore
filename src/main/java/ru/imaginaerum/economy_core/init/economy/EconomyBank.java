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

public final class EconomyBank {

    public static int balance(Player p) {
        return p.getData(EconomyAttachments.BALANCE.get());
    }
    /** Кладёт в инвентарь; что не влезло, выбрасывает перед игроком. */
    public static void giveOrDrop(Player p, ItemStack stack) {
        if (stack.isEmpty()) return;
        p.getInventory().add(stack);               // сначала в стеки, потом в свободные слоты
        if (!stack.isEmpty()) {
            ItemEntity drop = p.drop(stack.copy(), false, true);
            if (drop != null) {
                drop.setPickUpDelay(10);           // подобрать можно почти сразу
            }
            stack.setCount(0);
        }
        syncInventory(p);
    }
    /** Полная пересылка инвентаря клиенту (при открытом чужом меню обычные обновления слотов теряются). */
    public static void syncInventory(Player p) {
        if (p instanceof ServerPlayer) {
            p.inventoryMenu.sendAllDataToRemote();
        }
    }
    private static void setBalance(Player p, int v) {
        p.setData(EconomyAttachments.BALANCE.get(), Math.max(0, v));
    }
    /** Сколько изумрудов влезет в основной инвентарь (36 слотов). */
    public static int emeraldSpace(Player p) {
        int space = 0;
        for (ItemStack s : p.getInventory().items) {
            if (s.isEmpty()) {
                space += 64;
            } else if (s.is(Items.EMERALD)) {
                space += Math.max(0, s.getMaxStackSize() - s.getCount());
            }
        }
        return space;
    }

    /** Выдаёт изумруды: сколько влезет, в инвентарь, остаток зачисляется на счёт. */
    public static void giveEmeralds(Player p, int amount) {
        if (amount <= 0) return;
        int fit = Math.min(amount, emeraldSpace(p));

        int left = fit;
        while (left > 0) {
            int n = Math.min(64, left);
            p.getInventory().add(new ItemStack(Items.EMERALD, n));
            left -= n;
        }

        int rest = amount - fit;
        if (rest > 0) {
            long nb = (long) balance(p) + rest;
            setBalance(p, (int) Math.min(nb, Integer.MAX_VALUE));
            feedback(p, Component.translatable("message.economy_core.to_account", rest));
        }
        syncInventory(p);
    }
    public static int inInventory(Player p) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) {
            if (s.is(Items.EMERALD)) n += s.getCount();
        }
        return n;
    }

    /** Все изумруды игрока: в инвентаре + на счёте. */
    public static int total(Player p) {
        long t = (long) inInventory(p) + balance(p);
        return (int) Math.min(t, Integer.MAX_VALUE);
    }

    public static boolean isEmerald(ItemCost cost) {
        return cost.item().value() == Items.EMERALD;
    }

    /** Списывает изумруды: сначала из инвентаря, остаток со счёта. */
    public static void spend(Player p, int amount) {
        int left = amount;
        for (ItemStack s : p.getInventory().items) {
            if (left <= 0) break;
            if (s.is(Items.EMERALD)) {
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
        }
        if (left > 0) setBalance(p, balance(p) - left);
    }

    /** Кладёт на счёт всю стопку. */
    public static void deposit(Player p, ItemStack stack) {
        int space = Integer.MAX_VALUE - balance(p);
        int amount = Math.min(stack.getCount(), space);
        if (amount <= 0) return;
        stack.shrink(amount);
        setBalance(p, balance(p) + amount);
        feedback(p, Component.translatable("message.economy_core.deposit", amount));
    }

    /** Снимает со счёта до 64 изумрудов. */
    public static void withdraw(Player p) {
        int n = Math.min(balance(p), 64);
        if (n <= 0) {
            feedback(p, Component.translatable("message.economy_core.empty"));
            return;
        }
        n = Math.min(n, emeraldSpace(p));
        if (n <= 0) {
            feedback(p, Component.translatable("message.economy_core.no_room"));
            return;
        }
        setBalance(p, balance(p) - n);
        int left = n;
        while (left > 0) {
            int k = Math.min(64, left);
            p.getInventory().add(new ItemStack(Items.EMERALD, k));
            left -= k;
        }
        syncInventory(p);
        feedback(p, Component.translatable("message.economy_core.withdraw", n));
    }

    /** Для пакета с клиента: Shift + пустые руки. */
    public static void tryWithdraw(ServerPlayer p) {
        if (p.isShiftKeyDown() && p.getMainHandItem().isEmpty() && p.getOffhandItem().isEmpty()) {
            withdraw(p);
        }
    }
    /** Выдаёт валюту: изумруды идут через счёт, остальное в инвентарь, излишек выбрасывается. */
    public static void giveCurrency(Player p, Item currency, int amount) {
        if (amount <= 0) return;
        if (currency == Items.EMERALD) {
            giveEmeralds(p, amount);
            return;
        }
        int max = new ItemStack(currency).getMaxStackSize();
        int left = amount;
        while (left > 0) {
            int n = Math.min(max, left);
            giveOrDrop(p, new ItemStack(currency, n));
            left -= n;
        }
    }
    private static void feedback(Player p, Component msg) {
        p.displayClientMessage(msg, true); // над хотбаром
        p.level().playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    private EconomyBank() {}
}