package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideButcher {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.BUTCHER) return;
        TradeUtil.clear(e);

        TradeUtil.add(e, 1, Items.CHICKEN, 14, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.COOKED_CHICKEN, 5, 16, 1);

        TradeUtil.add(e, 2, Items.PORKCHOP, 7, Items.EMERALD, 1, 16, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.COOKED_PORKCHOP, 5, 16, 5);
        TradeUtil.add2(e, 2, Items.EMERALD, 1, Items.COAL, 2, Items.COOKED_PORKCHOP, 8, 16, 5);

        TradeUtil.add(e, 3, Items.MUTTON, 7, Items.EMERALD, 1, 16, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 1, Items.COOKED_MUTTON, 5, 16, 5);

        TradeUtil.add(e, 4, Items.BEEF, 10, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 1, Items.COOKED_BEEF, 5, 12, 10);
        TradeUtil.add2(e, 4, Items.EMERALD, 2, Items.SWEET_BERRIES, 6, Items.RABBIT_STEW, 2, 12, 15);

        TradeUtil.add(e, 5, Items.SWEET_BERRIES, 10, Items.EMERALD, 1, 12, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 6, Items.RABBIT_STEW, 3, 8, 30);
    }
}