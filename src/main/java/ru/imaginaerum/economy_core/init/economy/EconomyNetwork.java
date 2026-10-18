package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.economy.replace_trades.*;
import ru.imaginaerum.economy_core.init.tride.*;

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
        r.playToClient(SyncOffersPayload.TYPE, SyncOffersPayload.CODEC,
                (payload, ctx) -> ClientOfferSync.handle(payload));
        r.playToServer(SetTradePricePayload.TYPE, SetTradePricePayload.CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp && sp.hasPermissions(2)) {
                JsonVillagerTrades.setPrice(payload.profession(), payload.level(), payload.idx(), payload.count());
            }
        });
        // ---- редактор того, что торговцы покупают у игрока (/economycore buy) ----
        r.playToClient(OpenBuyEditorPayload.TYPE, OpenBuyEditorPayload.CODEC,
                (p, ctx) -> BuyEditorClient.open(p.rows()));
        r.playToServer(SellEditPayload.TYPE, SellEditPayload.CODEC, SellEditHandler::handle);
        r.playToClient(OpenSellEditorPayload.TYPE, OpenSellEditorPayload.CODEC,
                (payload, ctx) -> SellEditorClient.open(payload.rows()));
        r.playToServer(TradeEditPayload.TYPE, TradeEditPayload.CODEC, (p, ctx) -> {
            if (!(ctx.player() instanceof ServerPlayer sp) || !sp.hasPermissions(2)) return;
            switch (p.action()) {
                case TradeEditPayload.DELETE ->
                        JsonVillagerTrades.delete(p.profession(), p.level(), p.idx());
                case TradeEditPayload.ADD ->
                        JsonVillagerTrades.add(p.profession(), p.level(), p.resultId(), p.resultCount(),
                                p.priceId(), p.priceCount(), p.maxUses(), p.xp());
                case TradeEditPayload.RESET -> JsonVillagerTrades.reset(p.profession());
                case TradeEditPayload.EDIT -> {
                    // при успехе клиент уже показывает новые значения, список не пересылаем
                    // (иначе окно пересоберётся и слетит фокус поля)
                    if (JsonVillagerTrades.update(p.profession(), p.level(), p.idx(), p.resultId(),
                            p.resultCount(), p.priceId(), p.priceCount(), p.maxUses(), p.xp())) {
                        return;
                    }
                }
                default -> { }
            }
            // Всегда отправляем свежий список, чтобы номера торгов у клиента совпали с сервером
            PacketDistributor.sendToPlayer(sp, new OpenSellEditorPayload(JsonVillagerTrades.rows()));
        });
    }
}