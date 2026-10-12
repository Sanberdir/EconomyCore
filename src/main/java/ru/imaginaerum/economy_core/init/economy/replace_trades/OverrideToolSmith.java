package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideToolSmith {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.TOOLSMITH) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.COAL, 15, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.STONE_AXE, 1, 12, 1);

        TradeUtil.add(e, 2, Items.IRON_INGOT, 4, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 5, Items.IRON_PICKAXE, 1, 12, 5);

        TradeUtil.add(e, 3, Items.FLINT, 30, Items.EMERALD, 1, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 4, Items.IRON_SHOVEL, 1, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 4, Items.IRON_INGOT, 2, Items.IRON_SHOVEL, 1, 12, 10);

        TradeUtil.add(e, 4, Items.DIAMOND, 1, Items.EMERALD, 6, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 8, Items.IRON_AXE, 1, 12, 15);

        TradeUtil.add2(e, 5, Items.EMERALD, 8, Items.DIAMOND, 2, Items.DIAMOND_PICKAXE, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 13, Items.DIAMOND_PICKAXE, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 10, Items.DIAMOND_AXE, 1, 3, 30);
    }
}