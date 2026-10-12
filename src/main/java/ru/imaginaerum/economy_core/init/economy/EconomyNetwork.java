package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.tride.SyncPricesPayload;
import ru.imaginaerum.economy_core.init.tride.TraderPrices;

@EventBusSubscriber(modid = EconomyCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EconomyNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var r = event.registrar("1");

        r.playToServer(WithdrawPayload.TYPE, WithdrawPayload.CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                EconomyBank.tryWithdraw(sp);
            }
        });

        r.playToClient(SyncPricesPayload.TYPE, SyncPricesPayload.CODEC,
                (payload, ctx) -> TraderPrices.load(payload.entries()));
    }
}