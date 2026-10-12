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

        TradeUtil.add(e, 1, coin, 40, Items.BREAD, 6, 16, 1);
        TradeUtil.add(e, 2, coin, 120, Items.GOLDEN_CARROT, 4, 12, 5);
        TradeUtil.add(e, 3, coin, 60, Items.PUMPKIN_PIE, 6, 12, 10);
        TradeUtil.add(e, 4, coin, 90, Items.CAKE, 1, 8, 10);
        TradeUtil.add(e, 5, coin, 150, Items.GLISTERING_MELON_SLICE, 3, 8, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 12, Items.GOLDEN_APPLE, 1, 4, 30);
    }
}