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

        TradeUtil.add(e, 1, coin, 300, Items.ARROW, 16, 16, 1);
        TradeUtil.add(e, 1, coin, 60, Items.STICK, 8, 16, 2);
        TradeUtil.add(e, 1, coin, 50, Items.FEATHER, 6, 16, 2);
        TradeUtil.add(e, 1, coin, 40, Items.FLINT, 4, 16, 2);

        TradeUtil.add(e, 2, coin, 250, Items.BOW, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 90, Items.STRING, 6, 12, 6);
        TradeUtil.add(e, 2, coin, 120, Items.ARROW, 32, 12, 6);

        TradeUtil.add(e, 3, coin, 400, Items.CROSSBOW, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 150, Items.SPECTRAL_ARROW, 8, 12, 10);
        TradeUtil.add(e, 3, coin, 180, Items.TIPPED_ARROW, 4, 12, 10);

        TradeUtil.add(e, 4, coin, 200, Items.TIPPED_ARROW, 8, 12, 15);
        TradeUtil.add(e, 4, coin, 260, Items.BOW, 1, 8, 15);
        TradeUtil.add(e, 4, coin, 320, Items.CROSSBOW, 1, 8, 15);

        TradeUtil.add(e, 5, coin, 220, Items.SPECTRAL_ARROW, 16, 8, 30);
        TradeUtil.add(e, 5, coin, 400, Items.TIPPED_ARROW, 16, 6, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 24, Items.SPECTRAL_ARROW, 64, 4, 30);
    }
}