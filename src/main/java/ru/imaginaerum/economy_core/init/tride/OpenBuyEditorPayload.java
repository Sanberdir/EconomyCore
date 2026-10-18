package ru.imaginaerum.economy_core.init.tride;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

import java.util.List;

/** Сервер -> клиент: открыть/обновить редактор продажи. */
public record OpenBuyEditorPayload(List<SellRow> rows) implements CustomPacketPayload {

    public static final Type<OpenBuyEditorPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "open_buy_editor"));

    public static final StreamCodec<ByteBuf, OpenBuyEditorPayload> CODEC =
            SellRow.CODEC.apply(ByteBufCodecs.list())
                    .map(OpenBuyEditorPayload::new, OpenBuyEditorPayload::rows);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}