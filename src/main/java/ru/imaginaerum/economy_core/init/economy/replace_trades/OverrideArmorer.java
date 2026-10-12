package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideArmorer {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.ARMORER) return;
        TradeUtil.clear(e);

        // ---------- Уровень 1: Новичок ----------
        TradeUtil.add(e, 1, Items.COAL, 15, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 5, Items.IRON_HELMET, 1, 12, 1);
        TradeUtil.add(e, 1, Items.EMERALD, 4, Items.IRON_BOOTS, 1, 12, 1);
        // Слабо зачарованный железный шлем
        TradeUtil.addEnchanted(e, 1, Items.EMERALD, 9, Items.IRON_HELMET, 5, 10, 6, 3);

        // ---------- Уровень 2: Ученик ----------
        TradeUtil.add(e, 2, Items.IRON_INGOT, 4, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 9, Items.IRON_CHESTPLATE, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 7, Items.IRON_LEGGINGS, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.CHAINMAIL_BOOTS, 1, 12, 5);
        // Зачарованные железные поножи и нагрудник
        TradeUtil.addEnchanted(e, 2, Items.EMERALD, 14, Items.IRON_LEGGINGS, 8, 14, 6, 8);
        TradeUtil.addEnchanted(e, 2, Items.EMERALD, 18, Items.IRON_CHESTPLATE, 8, 14, 6, 8);

        // ---------- Уровень 3: Подмастерье ----------
        TradeUtil.add(e, 3, Items.LAVA_BUCKET, 1, Items.EMERALD, 2, 12, 10);
        TradeUtil.add(e, 3, Items.DIAMOND, 1, Items.EMERALD, 4, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 5, Items.IRON_INGOT, 3, Items.IRON_BOOTS, 1, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 5, Items.SHIELD, 1, 12, 10);
        // Зачарованные кольчужные и железные ботинки
        TradeUtil.addEnchanted(e, 3, Items.EMERALD, 16, Items.CHAINMAIL_CHESTPLATE, 10, 16, 6, 12);
        TradeUtil.addEnchanted(e, 3, Items.EMERALD, 14, Items.IRON_BOOTS, 10, 16, 6, 12);

        // ---------- Уровень 4: Эксперт ----------
        TradeUtil.add(e, 4, Items.DIAMOND, 1, Items.EMERALD, 6, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 12, Items.DIAMOND_BOOTS, 1, 3, 15);
        // Зачарованные алмазные поножи (твой торг) и ботинки
        TradeUtil.addEnchanted(e, 4, Items.EMERALD, 20, Items.DIAMOND_LEGGINGS, 5, 19, 3, 15);
        TradeUtil.addEnchanted(e, 4, Items.EMERALD, 24, Items.DIAMOND_BOOTS, 12, 19, 3, 15);
        // Зачарованный железный комплект за алмазы
        TradeUtil.addEnchanted(e, 4, Items.DIAMOND, 3, Items.IRON_CHESTPLATE, 15, 19, 4, 15);

        // ---------- Уровень 5: Мастер ----------
        TradeUtil.add(e, 5, Items.EMERALD, 18, Items.DIAMOND_CHESTPLATE, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 16, Items.DIAMOND_HELMET, 1, 3, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 10, Items.DIAMOND, 2, Items.DIAMOND_LEGGINGS, 1, 3, 30);
        // Зачарованные алмазные шлем и нагрудник
        TradeUtil.addEnchanted(e, 5, Items.EMERALD, 34, Items.DIAMOND_HELMET, 15, 19, 3, 30);
        TradeUtil.addEnchanted(e, 5, Items.EMERALD, 44, Items.DIAMOND_CHESTPLATE, 15, 19, 3, 30);
        // Топовое: оплата алмазами
        TradeUtil.addEnchanted(e, 5, Items.DIAMOND, 8, Items.DIAMOND_CHESTPLATE, 19, 19, 2, 30);
        TradeUtil.addEnchanted(e, 5, Items.DIAMOND, 6, Items.DIAMOND_LEGGINGS, 19, 19, 2, 30);
    }
}