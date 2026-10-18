package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SellEditHandler {

    public static void handle(SellEditPayload p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp) || !sp.hasPermissions(2)) return;

        boolean ok = switch (p.action()) {
            case SellEditPayload.SET -> TraderPriceStore.update(p.idx(), p.target(), p.count(),
                    p.price(), p.currency(), p.xp());
            case SellEditPayload.DELETE -> TraderPriceStore.delete(p.idx());
            case SellEditPayload.ADD -> TraderPriceStore.add(p.target(), p.count(), p.price(), p.currency());
            case SellEditPayload.RESET -> TraderPriceStore.reset();
            default -> false;
        };

        if (ok) {   // всем клиентам новые правила (для подсказок и диалога продажи)
            PacketDistributor.sendToAllPlayers(new SyncPricesPayload(TraderPrices.raw()));
        }
        if (p.action() != SellEditPayload.SET || !ok) {   // перерисовать редактор у автора
            PacketDistributor.sendToPlayer(sp, new OpenBuyEditorPayload(TraderPriceStore.rows()));
        }
    }

    private SellEditHandler() {}
}