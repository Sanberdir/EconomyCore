package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EconomyNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(WithdrawPayload.TYPE, WithdrawPayload.CODEC,
                (payload, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer sp) {
                        EconomyBank.tryWithdraw(sp);
                    }
                });
    }
}