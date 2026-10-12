package ru.imaginaerum.economy_core.init.tride;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class TraderPriceEvents {

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent e) {
        e.addListener(new TraderPriceLoader());
    }

    /** При входе игрока и после /reload отправляем ему правила. */
    @SubscribeEvent
    public static void onSync(OnDatapackSyncEvent e) {
        SyncPricesPayload payload = new SyncPricesPayload(TraderPrices.raw());
        e.getRelevantPlayers().forEach(p -> PacketDistributor.sendToPlayer(p, payload));
    }
}