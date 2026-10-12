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

        TradeUtil.add(e, 1, coin, 30, Items.BRICK, 10, 16, 1);
        TradeUtil.add(e, 2, coin, 30, Items.CHISELED_STONE_BRICKS, 4, 16, 5);
        TradeUtil.add(e, 2, coin, 25, Items.TERRACOTTA, 4, 16, 5);
        TradeUtil.add(e, 3, coin, 20, Items.POLISHED_ANDESITE, 4, 16, 10);
        TradeUtil.add(e, 4, coin, 8, Items.TERRACOTTA, 1, 12, 15);
        TradeUtil.add(e, 5, coin, 60, Items.QUARTZ_PILLAR, 4, 12, 30);
        TradeUtil.add(e, 5, coin, 20, Items.QUARTZ_BLOCK, 1, 12, 30);
    }
}