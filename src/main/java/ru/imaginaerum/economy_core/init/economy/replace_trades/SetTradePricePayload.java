package ru.imaginaerum.economy_core.init.economy.replace_trades;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

/** Клиент -> сервер: новая цена торга. count = 0 сбрасывает к значению из JSON. */
public record SetTradePricePayload(String profession, int level, int idx, int count)
        implements CustomPacketPayload {

    public static final Type<SetTradePricePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "set_trade_price"));

    public static final StreamCodec<ByteBuf, SetTradePricePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SetTradePricePayload::profession,
            ByteBufCodecs.VAR_INT, SetTradePricePayload::level,
            ByteBufCodecs.VAR_INT, SetTradePricePayload::idx,
            ByteBufCodecs.VAR_INT, SetTradePricePayload::count,
            SetTradePricePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}