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
public class OverrideFisherman {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FISHERMAN) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 80, Items.COOKED_COD, 1, 16, 1);
        TradeUtil.add(e, 1, coin, 50, Items.COD, 1, 16, 2);
        TradeUtil.add(e, 1, coin, 20, Items.STRING, 1, 16, 2);

        TradeUtil.add(e, 2, coin, 80, Items.COOKED_SALMON, 1, 16, 5);
        TradeUtil.add(e, 2, coin, 60, Items.SALMON, 1, 16, 6);
        TradeUtil.add(e, 2, coin, 50, Items.INK_SAC, 1, 16, 6);

        TradeUtil.add(e, 3, coin, 200, Items.CAMPFIRE, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 250, Items.FISHING_ROD, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 90, Items.TROPICAL_FISH, 1, 12, 10);

        TradeUtil.add(e, 4, coin, 95, Items.TROPICAL_FISH, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 120, Items.PUFFERFISH, 1, 12, 15);

        TradeUtil.add(e, 5, coin, 350, Items.PUFFERFISH, 1, 12, 30);
        TradeUtil.add(e, 5, coin, 1500, Items.HEART_OF_THE_SEA, 1, 6, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 12, Items.NAUTILUS_SHELL, 1, 6, 30);
    }
}