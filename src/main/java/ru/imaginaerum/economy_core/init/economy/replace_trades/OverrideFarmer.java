package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideFarmer {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FARMER) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.WHEAT, 20, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.BREAD, 6, 16, 1);

        TradeUtil.add(e, 2, Items.CARROT, 22, Items.EMERALD, 1, 16, 5);
        TradeUtil.add2(e, 2, Items.EMERALD, 3, Items.GOLD_INGOT, 1, Items.GOLDEN_CARROT, 4, 12, 5);

        TradeUtil.add(e, 3, Items.PUMPKIN, 6, Items.EMERALD, 1, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 2, Items.WHEAT, 10, Items.PUMPKIN_PIE, 6, 12, 10);

        TradeUtil.add(e, 4, Items.MELON_SLICE, 14, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 3, Items.CAKE, 1, 8, 10);

        TradeUtil.add(e, 5, Items.EMERALD, 6, Items.GLISTERING_MELON_SLICE, 3, 8, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 4, Items.GOLD_INGOT, 2, Items.GOLDEN_APPLE, 1, 4, 30);
    }
}