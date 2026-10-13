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

        TradeUtil.add(e, 1, coin, 50, Items.STONE_AXE, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 40, Items.STONE_PICKAXE, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 30, Items.STONE_SHOVEL, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 35, Items.STONE_HOE, 1, 12, 1);

        TradeUtil.add(e, 2, coin, 200, Items.IRON_PICKAXE, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 180, Items.IRON_AXE, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 150, Items.IRON_SHOVEL, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 160, Items.IRON_HOE, 1, 12, 5);

        TradeUtil.add(e, 3, coin, 120, Items.IRON_SHOVEL, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 140, Items.SHIELD, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 220, Items.IRON_PICKAXE, 1, 8, 10);
        TradeUtil.add(e, 3, coin, 200, Items.IRON_AXE, 1, 8, 10);

        TradeUtil.add(e, 4, coin, 130, Items.IRON_AXE, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 250, Items.DIAMOND_SHOVEL, 1, 8, 15);
        TradeUtil.add(e, 4, coin, 320, Items.DIAMOND_HOE, 1, 8, 15);
        TradeUtil.add(e, 4, coin, 280, Items.SHIELD, 1, 8, 15);

        TradeUtil.add(e, 5, coin, 300, Items.DIAMOND_PICKAXE, 1, 3, 30);
        TradeUtil.add(e, 5, coin, 350, Items.DIAMOND_AXE, 1, 3, 30);
        TradeUtil.add(e, 5, coin, 380, Items.DIAMOND_SHOVEL, 1, 3, 30);
        TradeUtil.add(e, 5, coin, 400, Items.DIAMOND_HOE, 1, 3, 30);
        TradeUtil.add2(e, 5, coin, 300, Items.EMERALD, 12, Items.DIAMOND_PICKAXE, 1, 3, 30);
    }
}