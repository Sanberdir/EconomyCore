package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.items.EcoItems;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideArmorer {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.ARMORER) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        // 1
        TradeUtil.add(e, 1, coin, 200, Items.IRON_HELMET, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 160, Items.IRON_BOOTS, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 140, Items.CHAINMAIL_HELMET, 1, 12, 1);
        TradeUtil.addEnchanted(e, 1, coin, 450, Items.IRON_HELMET, 5, 10, 6, 3);

        // 2
        TradeUtil.add(e, 2, coin, 320, Items.IRON_CHESTPLATE, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 280, Items.IRON_LEGGINGS, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 110, Items.CHAINMAIL_BOOTS, 1, 12, 5);
        TradeUtil.addEnchanted(e, 2, coin, 600, Items.IRON_LEGGINGS, 8, 14, 6, 8);
        TradeUtil.addEnchanted(e, 2, coin, 700, Items.IRON_CHESTPLATE, 8, 14, 6, 8);

        // 3
        TradeUtil.add(e, 3, coin, 100, Items.SHIELD, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 300, Items.CHAINMAIL_CHESTPLATE, 1, 12, 10);
        TradeUtil.addEnchanted(e, 3, coin, 800, Items.CHAINMAIL_CHESTPLATE, 10, 16, 6, 12);
        TradeUtil.addEnchanted(e, 3, coin, 650, Items.IRON_BOOTS, 10, 16, 6, 12);

        // 4: алмаз, изумруды
        TradeUtil.add(e, 4, Items.EMERALD, 24, Items.DIAMOND_BOOTS, 1, 3, 15);
        TradeUtil.addEnchanted(e, 4, Items.EMERALD, 45, Items.DIAMOND_LEGGINGS, 5, 19, 3, 15);
        TradeUtil.addEnchanted(e, 4, Items.EMERALD, 38, Items.DIAMOND_BOOTS, 12, 19, 3, 15);
        TradeUtil.add2(e, 4, coin, 1000, Items.EMERALD, 5, Items.IRON_CHESTPLATE, 1, 4, 15);

        // 5
        TradeUtil.add(e, 5, Items.EMERALD, 48, Items.DIAMOND_CHESTPLATE, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 30, Items.DIAMOND_HELMET, 1, 3, 30);
        TradeUtil.add2(e, 5, coin, 400, Items.EMERALD, 20, Items.DIAMOND_LEGGINGS, 1, 3, 30);
        TradeUtil.addEnchanted(e, 5, Items.EMERALD, 55, Items.DIAMOND_HELMET, 15, 19, 3, 30);
        TradeUtil.addEnchanted(e, 5, Items.EMERALD, 70, Items.DIAMOND_CHESTPLATE, 15, 19, 3, 30);
        TradeUtil.addEnchanted(e, 5, Items.EMERALD, 90, Items.DIAMOND_CHESTPLATE, 19, 19, 2, 30);
        TradeUtil.addEnchanted(e, 5, Items.EMERALD, 80, Items.DIAMOND_LEGGINGS, 19, 19, 2, 30);
    }
}