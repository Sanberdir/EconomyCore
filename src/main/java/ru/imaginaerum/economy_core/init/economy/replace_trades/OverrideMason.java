package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideMason {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.MASON) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.CLAY_BALL, 10, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.BRICK, 10, 16, 1);

        TradeUtil.add(e, 2, Items.STONE, 20, Items.EMERALD, 1, 16, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.CHISELED_STONE_BRICKS, 4, 16, 5);
        TradeUtil.add2(e, 2, Items.EMERALD, 1, Items.CLAY_BALL, 5, Items.TERRACOTTA, 4, 16, 5);

        TradeUtil.add(e, 3, Items.GRANITE, 16, Items.EMERALD, 1, 16, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 1, Items.POLISHED_ANDESITE, 4, 16, 10);

        TradeUtil.add(e, 4, Items.QUARTZ, 12, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 1, Items.TERRACOTTA, 1, 12, 15);

        TradeUtil.add2(e, 5, Items.EMERALD, 2, Items.QUARTZ, 6, Items.QUARTZ_PILLAR, 4, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 1, Items.QUARTZ_PILLAR, 1, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 1, Items.QUARTZ_BLOCK, 1, 12, 30);
    }
}