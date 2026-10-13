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
public class OverrideCleric {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.CLERIC) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 30, Items.REDSTONE, 1, 16, 1);
        TradeUtil.add(e, 1, coin, 25, Items.GUNPOWDER, 2, 16, 2);
        TradeUtil.add(e, 1, coin, 20, Items.ROTTEN_FLESH, 4, 16, 2);

        TradeUtil.add(e, 2, coin, 40, Items.LAPIS_LAZULI, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 60, Items.GLOWSTONE_DUST, 2, 12, 6);
        TradeUtil.add(e, 2, coin, 50, Items.SUGAR, 4, 12, 6);

        TradeUtil.add(e, 3, coin, 80, Items.GLOWSTONE, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 120, Items.RABBIT_FOOT, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 150, Items.BLAZE_POWDER, 2, 12, 10);

        TradeUtil.add(e, 4, coin, 200, Items.ENDER_PEARL, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 180, Items.GHAST_TEAR, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 220, Items.SCULK, 4, 12, 15);

        TradeUtil.add(e, 5, coin, 300, Items.EXPERIENCE_BOTTLE, 1, 12, 30);
        TradeUtil.add(e, 5, coin, 350, Items.ENDER_EYE, 1, 8, 30);
        TradeUtil.add(e, 5, coin, 400, Items.ENCHANTED_GOLDEN_APPLE, 1, 6, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 20, Items.EXPERIENCE_BOTTLE, 1, 6, 30);
    }
}