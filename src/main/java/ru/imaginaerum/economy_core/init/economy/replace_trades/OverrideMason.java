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
public class OverrideMason {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.MASON) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 120, Items.BRICK, 10, 16, 1);
        TradeUtil.add(e, 1, coin, 100, Items.CLAY_BALL, 8, 16, 2);
        TradeUtil.add(e, 1, coin, 100, Items.STONE, 10, 16, 2);
        TradeUtil.add(e, 1, coin, 250, Items.STONE_BRICKS, 8, 16, 2);

        TradeUtil.add(e, 2, coin, 300, Items.CHISELED_STONE_BRICKS, 4, 16, 5);
        TradeUtil.add(e, 2, coin, 100, Items.TERRACOTTA, 4, 16, 5);
        TradeUtil.add(e, 2, coin, 100, Items.POLISHED_GRANITE, 4, 16, 5);
        TradeUtil.add(e, 2, coin, 100, Items.POLISHED_DIORITE, 4, 16, 5);

        TradeUtil.add(e, 3, coin, 200, Items.POLISHED_ANDESITE, 4, 16, 10);
        TradeUtil.add(e, 3, coin, 200, Items.POLISHED_DEEPSLATE, 4, 16, 10);
        TradeUtil.add(e, 3, coin, 110, Items.DEEPSLATE_BRICKS, 4, 16, 10);
        TradeUtil.add(e, 3, coin, 100, Items.MUD_BRICKS, 4, 16, 10);

        TradeUtil.add(e, 4, coin, 10, Items.TERRACOTTA, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 140, Items.GRAY_GLAZED_TERRACOTTA, 4, 12, 15);
        TradeUtil.add(e, 4, coin, 160, Items.QUARTZ_BLOCK, 4, 12, 15);
        TradeUtil.add(e, 4, coin, 180, Items.CHISELED_QUARTZ_BLOCK, 2, 12, 15);

        TradeUtil.add(e, 5, coin, 200, Items.QUARTZ_PILLAR, 4, 12, 30);
        TradeUtil.add(e, 5, coin, 100, Items.QUARTZ_BLOCK, 1, 12, 30);
        TradeUtil.add(e, 5, coin, 250, Items.QUARTZ_BRICKS, 4, 12, 30);
        TradeUtil.add(e, 5, coin, 300, Items.CHISELED_QUARTZ_BLOCK, 4, 8, 30);
    }
}