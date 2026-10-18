package ru.imaginaerum.economy_core.init.economy.replace_trades;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

import java.util.List;

/** Сервер -> клиент: открыть редактор цен. */
public record OpenSellEditorPayload(List<TradeRow> rows) implements CustomPacketPayload {

    public static final Type<OpenSellEditorPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "open_sell_editor"));

    public static final StreamCodec<ByteBuf, OpenSellEditorPayload> CODEC =
            TradeRow.CODEC.apply(ByteBufCodecs.list())
                    .map(OpenSellEditorPayload::new, OpenSellEditorPayload::rows);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}