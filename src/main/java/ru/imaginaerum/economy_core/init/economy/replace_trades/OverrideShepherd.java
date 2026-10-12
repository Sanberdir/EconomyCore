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
        TradeUtil.add(e, 2, coin, 40, Items.WHITE_DYE, 12, 16, 5);
        TradeUtil.add(e, 2, coin, 40, Items.WHITE_CARPET, 8, 16, 5);
        TradeUtil.add(e, 3, coin, 80, Items.WHITE_BED, 1, 12, 10);
        TradeUtil.add(e, 4, coin, 25, Items.WHITE_CARPET, 4, 12, 15);
        TradeUtil.add(e, 5, coin, 60, Items.PAINTING, 3, 12, 30);
        TradeUtil.add(e, 5, coin, 60, Items.WHITE_BANNER, 1, 12, 30);
    }
}