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
public class OverrideWeaponSmith {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.WEAPONSMITH) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        TradeUtil.add(e, 1, coin, 130, Items.IRON_AXE, 1, 12, 1);
        TradeUtil.add(e, 2, coin, 90, Items.IRON_SWORD, 1, 12, 5);
        TradeUtil.add(e, 3, coin, 120, Items.BOW, 1, 12, 10);
        TradeUtil.add(e, 3, coin, 100, Items.SHIELD, 1, 12, 10);

        TradeUtil.add(e, 4, Items.EMERALD, 20, Items.DIAMOND_AXE, 1, 3, 15);

        TradeUtil.add(e, 5, Items.EMERALD, 13, Items.DIAMOND_SWORD, 1, 3, 30);
        TradeUtil.add2(e, 5, coin, 160, Items.EMERALD, 8, Items.DIAMOND_SWORD, 1, 3, 30);
    }
}