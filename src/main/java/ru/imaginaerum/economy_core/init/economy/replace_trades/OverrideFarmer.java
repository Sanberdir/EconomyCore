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
public class OverrideFarmer {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FARMER) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 80, Items.BREAD, 1, 16, 1);
        TradeUtil.add(e, 1, coin, 30, Items.WHEAT, 6, 16, 2);
        TradeUtil.add(e, 1, coin, 25, Items.CARROT, 6, 16, 2);
        TradeUtil.add(e, 1, coin, 25, Items.POTATO, 6, 16, 2);

        TradeUtil.add(e, 2, coin, 120, Items.GOLDEN_CARROT, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 40, Items.BEETROOT, 6, 12, 5);
        TradeUtil.add(e, 2, coin, 60, Items.PUMPKIN, 2, 12, 5);
        TradeUtil.add(e, 2, coin, 50, Items.MELON_SLICE, 6, 12, 5);

        TradeUtil.add(e, 3, coin, 250, Items.PUMPKIN_PIE, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 90, Items.COOKIE, 8, 12, 10);
        TradeUtil.add(e, 3, coin, 70, Items.APPLE, 4, 12, 10);

        TradeUtil.add(e, 4, coin, 200, Items.CAKE, 1, 1, 10);
        TradeUtil.add(e, 4, coin, 140, Items.HONEY_BOTTLE, 2, 12, 15);
        TradeUtil.add(e, 4, coin, 160, Items.SWEET_BERRIES, 8, 12, 15);

        TradeUtil.add(e, 5, coin, 150, Items.GLISTERING_MELON_SLICE, 1, 8, 30);
        TradeUtil.add(e, 5, coin, 220, Items.HAY_BLOCK, 4, 8, 30);
        TradeUtil.add(e, 5, coin, 180, Items.GOLDEN_CARROT, 4, 8, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 12, Items.GOLDEN_APPLE, 1, 4, 30);
    }
}