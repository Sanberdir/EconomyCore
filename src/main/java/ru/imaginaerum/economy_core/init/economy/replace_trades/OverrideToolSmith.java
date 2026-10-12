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
public class OverrideToolSmith {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.TOOLSMITH) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 40, Items.STONE_AXE, 1, 12, 1);
        TradeUtil.add(e, 2, coin, 130, Items.IRON_PICKAXE, 1, 12, 5);
        TradeUtil.add(e, 3, coin, 50, Items.IRON_SHOVEL, 1, 12, 10);
        TradeUtil.add(e, 4, coin, 130, Items.IRON_AXE, 1, 12, 15);

        TradeUtil.add(e, 5, Items.EMERALD, 22, Items.DIAMOND_PICKAXE, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 22, Items.DIAMOND_AXE, 1, 3, 30);
        TradeUtil.add2(e, 5, coin, 300, Items.EMERALD, 12, Items.DIAMOND_PICKAXE, 1, 3, 30);
    }
}