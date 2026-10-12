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

        TradeUtil.add(e, 1, coin, 45, Items.COOKED_COD, 6, 16, 1);
        TradeUtil.add(e, 2, coin, 60, Items.COOKED_SALMON, 6, 16, 5);
        TradeUtil.add(e, 3, coin, 80, Items.CAMPFIRE, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 100, Items.FISHING_ROD, 1, 12, 10);
        TradeUtil.add(e, 4, coin, 40, Items.TROPICAL_FISH, 4, 12, 15);
        TradeUtil.add(e, 5, coin, 60, Items.PUFFERFISH, 3, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 12, Items.NAUTILUS_SHELL, 1, 6, 30);
    }
}