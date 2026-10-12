package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideShepherd {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.SHEPHERD) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.WHITE_WOOL, 18, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 2, Items.SHEARS, 1, 12, 1);

        TradeUtil.add(e, 2, Items.BLACK_WOOL, 12, Items.EMERALD, 1, 16, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.WHITE_DYE, 12, 16, 5);
        TradeUtil.add2(e, 2, Items.EMERALD, 1, Items.WHITE_WOOL, 4, Items.WHITE_CARPET, 8, 16, 5);

        TradeUtil.add(e, 3, Items.RED_WOOL, 12, Items.EMERALD, 1, 16, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 1, Items.WHITE_BED, 1, 12, 10);

        TradeUtil.add(e, 4, Items.BLUE_WOOL, 12, Items.EMERALD, 1, 16, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 2, Items.WHITE_CARPET, 4, 12, 15);

        TradeUtil.add2(e, 5, Items.EMERALD, 3, Items.STRING, 6, Items.PAINTING, 3, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 2, Items.PAINTING, 3, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 3, Items.WHITE_BANNER, 1, 12, 30);
    }
}