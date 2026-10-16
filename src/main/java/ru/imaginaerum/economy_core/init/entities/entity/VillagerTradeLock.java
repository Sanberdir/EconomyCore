package ru.imaginaerum.economy_core.init.entities.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import ru.imaginaerum.economy_core.init.economy.EconomyAttachments;

import java.util.HashMap;
import java.util.Map;

public final class VillagerTradeLock {

    private static boolean hasTrades(VillagerProfession p) {
        return p != VillagerProfession.NONE && p != VillagerProfession.NITWIT;
    }

    private static String key(VillagerProfession p) {
        return BuiltInRegistries.VILLAGER_PROFESSION.getKey(p).toString();
    }

    /** Вызывается прямо перед сменой профессии. */
    public static void remember(Villager v, VillagerProfession old) {
        if (!hasTrades(old)) return;
        MerchantOffers offers = v.getOffers();       // если ещё не было, сгенерируются сейчас и зафиксируются
        if (offers.isEmpty()) return;

        Map<String, MerchantOffers> mem = new HashMap<>(v.getData(EconomyAttachments.TRADE_MEMORY.get()));
        mem.put(key(old), offers);
        v.setData(EconomyAttachments.TRADE_MEMORY.get(), mem);
    }

    /** Вызывается сразу после смены профессии. */
    public static void restore(Villager v, VillagerProfession now) {
        if (!hasTrades(now)) return;
        MerchantOffers saved = v.getData(EconomyAttachments.TRADE_MEMORY.get()).get(key(now));
        if (saved != null) {
            v.overrideOffers(saved);
        }
    }

    private VillagerTradeLock() {}
}