package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideCartographer {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.CARTOGRAPHER) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.PAPER, 24, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 7, Items.MAP, 1, 12, 1);

        TradeUtil.add(e, 2, Items.GLASS_PANE, 11, Items.EMERALD, 1, 16, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 2, Items.COMPASS, 1, 12, 5);

        TradeUtil.add(e, 3, Items.COMPASS, 1, Items.EMERALD, 4, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 4, Items.ITEM_FRAME, 1, 12, 5);
        TradeUtil.add2(e, 3, Items.EMERALD, 4, Items.COMPASS, 1, Items.SPYGLASS, 1, 12, 10);

        TradeUtil.add(e, 4, Items.EMERALD, 8, Items.WHITE_BANNER, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 4, Items.BLACK_BANNER, 1, 12, 15);

        TradeUtil.add(e, 5, Items.EMERALD, 8, Items.GLOBE_BANNER_PATTERN, 1, 8, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 12, Items.SPYGLASS, 1, 8, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 8, Items.DIAMOND, 1, Items.GLOBE_BANNER_PATTERN, 1, 6, 30);
    }
}