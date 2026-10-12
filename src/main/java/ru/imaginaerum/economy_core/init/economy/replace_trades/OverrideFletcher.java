package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideFletcher {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FLETCHER) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.STICK, 32, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.ARROW, 16, 16, 1);

        TradeUtil.add(e, 2, Items.FLINT, 26, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 2, Items.BOW, 1, 12, 5);
        TradeUtil.add2(e, 2, Items.EMERALD, 2, Items.STICK, 8, Items.BOW, 1, 12, 5);

        TradeUtil.add(e, 3, Items.STRING, 14, Items.EMERALD, 1, 16, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 3, Items.CROSSBOW, 1, 12, 10);

        TradeUtil.add(e, 4, Items.FEATHER, 24, Items.EMERALD, 1, 16, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 1, Items.TIPPED_ARROW, 4, 12, 15);

        TradeUtil.add(e, 5, Items.TRIPWIRE_HOOK, 8, Items.EMERALD, 1, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 8, Items.SPECTRAL_ARROW, 16, 8, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 6, Items.GOLD_INGOT, 2, Items.SPECTRAL_ARROW, 16, 8, 30);
    }
}