package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideLeatherWorker {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.LEATHERWORKER) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.LEATHER, 6, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 3, Items.LEATHER_LEGGINGS, 1, 12, 1);

        TradeUtil.add(e, 2, Items.FLINT, 26, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 7, Items.LEATHER_CHESTPLATE, 1, 12, 5);
        TradeUtil.add2(e, 2, Items.EMERALD, 4, Items.LEATHER, 4, Items.LEATHER_CHESTPLATE, 1, 12, 5);

        TradeUtil.add(e, 3, Items.RABBIT_HIDE, 9, Items.EMERALD, 1, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 5, Items.LEATHER_HELMET, 1, 12, 10);

        TradeUtil.add(e, 4, Items.ARMADILLO_SCUTE, 4, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 6, Items.LEATHER_HORSE_ARMOR, 1, 12, 15);

        TradeUtil.add(e, 5, Items.EMERALD, 6, Items.SADDLE, 1, 12, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 6, Items.LEATHER, 8, Items.SADDLE, 1, 8, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 5, Items.LEATHER_BOOTS, 1, 12, 30);
    }
}