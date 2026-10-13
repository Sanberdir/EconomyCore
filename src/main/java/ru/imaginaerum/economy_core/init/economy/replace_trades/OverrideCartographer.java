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
public class OverrideCartographer {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.CARTOGRAPHER) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 180, Items.MAP, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 60, Items.PAPER, 8, 16, 2);
        TradeUtil.add(e, 1, coin, 50, Items.GLASS_PANE, 6, 16, 2);

        TradeUtil.add(e, 2, coin, 120, Items.COMPASS, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 90, Items.WRITABLE_BOOK, 1, 12, 6);
        TradeUtil.add(e, 2, coin, 70, Items.WHITE_WOOL, 6, 12, 6);

        TradeUtil.add(e, 3, coin, 100, Items.ITEM_FRAME, 1, 12, 5);
        TradeUtil.add(e, 3, coin, 350, Items.SPYGLASS, 1, 8, 10);
        TradeUtil.add(e, 3, coin, 130, Items.BOOK, 4, 12, 10);
        TradeUtil.add(e, 3, coin, 160, Items.PAINTING, 1, 12, 10);

        TradeUtil.add(e, 4, coin, 150, Items.WHITE_BANNER, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 150, Items.BLACK_BANNER, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 150, Items.RED_BANNER, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 150, Items.BLUE_BANNER, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 220, Items.ITEM_FRAME, 4, 8, 15);

        TradeUtil.add(e, 5, coin, 500, Items.GLOBE_BANNER_PATTERN, 1, 8, 30);
        TradeUtil.add(e, 5, coin, 400, Items.FILLED_MAP, 1, 8, 30);
        TradeUtil.add(e, 5, coin, 280, Items.ITEM_FRAME, 8, 6, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 14, Items.SPYGLASS, 1, 4, 30);
    }
}