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
public class OverrideFletcher {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FLETCHER) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 40, Items.ARROW, 16, 16, 1);
        TradeUtil.add(e, 2, coin, 120, Items.BOW, 1, 12, 5);
        TradeUtil.add(e, 3, coin, 180, Items.CROSSBOW, 1, 12, 10);
        TradeUtil.add(e, 4, coin, 100, Items.TIPPED_ARROW, 4, 12, 15);
        TradeUtil.add(e, 5, coin, 220, Items.SPECTRAL_ARROW, 16, 8, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 28, Items.SPECTRAL_ARROW, 64, 4, 30);
    }
}