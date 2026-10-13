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
public class OverrideShepherd {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.SHEPHERD) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 50, Items.SHEARS, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 30, Items.WHITE_WOOL, 6, 16, 2);
        TradeUtil.add(e, 1, coin, 40, Items.WHITE_DYE, 4, 16, 2);
        TradeUtil.add(e, 1, coin, 60, Items.LEAD, 1, 12, 2);

        TradeUtil.add(e, 2, coin, 40, Items.WHITE_DYE, 1, 16, 5);
        TradeUtil.add(e, 2, coin, 40, Items.WHITE_CARPET, 1, 16, 5);
        TradeUtil.add(e, 2, coin, 50, Items.BLACK_DYE, 4, 16, 5);
        TradeUtil.add(e, 2, coin, 60, Items.BLACK_WOOL, 6, 16, 5);

        TradeUtil.add(e, 3, coin, 80, Items.WHITE_BED, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 90, Items.RED_BED, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 100, Items.BLUE_BED, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 70, Items.WHITE_CARPET, 4, 12, 10);

        TradeUtil.add(e, 4, coin, 25, Items.WHITE_CARPET, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 120, Items.PAINTING, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 150, Items.WHITE_BANNER, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 150, Items.RED_BANNER, 1, 12, 15);

        TradeUtil.add(e, 5, coin, 60, Items.PAINTING, 1, 12, 30);
        TradeUtil.add(e, 5, coin, 60, Items.WHITE_BANNER, 1, 12, 30);
        TradeUtil.add(e, 5, coin, 220, Items.PAINTING, 4, 8, 30);
        TradeUtil.add(e, 5, coin, 260, Items.WHITE_BANNER, 4, 8, 30);
    }
}