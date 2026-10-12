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
public class OverrideButcher {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.BUTCHER) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 40, Items.COOKED_CHICKEN, 5, 16, 1);
        TradeUtil.add(e, 2, coin, 50, Items.COOKED_PORKCHOP, 5, 16, 5);
        TradeUtil.add(e, 3, coin, 50, Items.COOKED_MUTTON, 5, 16, 5);
        TradeUtil.add(e, 4, coin, 60, Items.COOKED_BEEF, 5, 12, 10);
        TradeUtil.add(e, 4, coin, 50, Items.RABBIT_STEW, 2, 12, 15);
        TradeUtil.add(e, 5, coin, 70, Items.RABBIT_STEW, 3, 8, 30);
    }
}