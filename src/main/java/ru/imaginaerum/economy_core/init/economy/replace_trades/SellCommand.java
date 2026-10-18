package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class SellCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("economycore")
                .then(Commands.literal("sell")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            PacketDistributor.sendToPlayer(p, new OpenSellEditorPayload(JsonVillagerTrades.rows()));
                            return 1;
                        }))
                .then(Commands.literal("buy")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            PacketDistributor.sendToPlayer(p,
                                    new ru.imaginaerum.economy_core.init.tride.OpenBuyEditorPayload(
                                            ru.imaginaerum.economy_core.init.tride.TraderPriceStore.rows()));
                            return 1;
                        })));
    }
}