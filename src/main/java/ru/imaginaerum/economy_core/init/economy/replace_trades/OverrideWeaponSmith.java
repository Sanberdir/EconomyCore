package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideWeaponSmith {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.WEAPONSMITH) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.COAL, 15, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 3, Items.IRON_AXE, 1, 12, 1);

        TradeUtil.add(e, 2, Items.IRON_INGOT, 4, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 7, Items.IRON_SWORD, 1, 12, 5);

        TradeUtil.add(e, 3, Items.FLINT, 24, Items.EMERALD, 1, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 6, Items.BOW, 1, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 5, Items.IRON_INGOT, 3, Items.IRON_SWORD, 1, 12, 10);

        TradeUtil.add(e, 4, Items.DIAMOND, 1, Items.EMERALD, 6, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 12, Items.DIAMOND_AXE, 1, 3, 15);

        TradeUtil.add2(e, 5, Items.EMERALD, 10, Items.DIAMOND, 2, Items.DIAMOND_SWORD, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 14, Items.DIAMOND_SWORD, 1, 3, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 4, Items.SHIELD, 1, 6, 30);
    }
}