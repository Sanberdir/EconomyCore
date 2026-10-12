package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideCleric {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.CLERIC) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.ROTTEN_FLESH, 32, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.REDSTONE, 2, 16, 1);

        TradeUtil.add(e, 2, Items.GOLD_INGOT, 3, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.LAPIS_LAZULI, 1, 12, 5);

        TradeUtil.add(e, 3, Items.RABBIT_FOOT, 2, Items.EMERALD, 1, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 4, Items.GLOWSTONE, 1, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 3, Items.REDSTONE, 4, Items.GLOWSTONE, 2, 12, 10);

        TradeUtil.add(e, 4, Items.GLASS_BOTTLE, 9, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 5, Items.ENDER_PEARL, 1, 12, 15);

        TradeUtil.add(e, 5, Items.NETHER_WART, 22, Items.EMERALD, 1, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 3, Items.EXPERIENCE_BOTTLE, 1, 12, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 6, Items.DIAMOND, 1, Items.EXPERIENCE_BOTTLE, 3, 8, 30);
    }
}