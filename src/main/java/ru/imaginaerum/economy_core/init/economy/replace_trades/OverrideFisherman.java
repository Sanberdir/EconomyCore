package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideFisherman {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FISHERMAN) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.STRING, 20, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.COOKED_COD, 6, 16, 1);

        TradeUtil.add(e, 2, Items.COD, 15, Items.EMERALD, 1, 16, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.COOKED_SALMON, 6, 16, 5);

        TradeUtil.add(e, 3, Items.SALMON, 13, Items.EMERALD, 1, 16, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 3, Items.CAMPFIRE, 1, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 2, Items.STRING, 6, Items.FISHING_ROD, 1, 12, 10);

        TradeUtil.add(e, 4, Items.TROPICAL_FISH, 6, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 7, Items.FISHING_ROD, 1, 12, 15);

        TradeUtil.add(e, 5, Items.PUFFERFISH, 4, Items.EMERALD, 1, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 10, Items.NAUTILUS_SHELL, 1, 6, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 5, Items.GOLD_INGOT, 2, Items.NAUTILUS_SHELL, 1, 6, 30);
    }
}