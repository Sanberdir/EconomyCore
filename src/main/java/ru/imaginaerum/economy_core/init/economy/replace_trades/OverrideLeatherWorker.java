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
public class OverrideLeatherWorker {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.LEATHERWORKER) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 80, Items.LEATHER_LEGGINGS, 1, 12, 1);
        TradeUtil.add(e, 2, coin, 175, Items.LEATHER_CHESTPLATE, 1, 12, 5);
        TradeUtil.add(e, 3, coin, 125, Items.LEATHER_HELMET, 1, 12, 10);
        TradeUtil.add(e, 4, coin, 150, Items.LEATHER_HORSE_ARMOR, 1, 12, 15);
        TradeUtil.add(e, 5, coin, 100, Items.LEATHER_BOOTS, 1, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 16, Items.SADDLE, 1, 6, 30);
    }
}