package ru.imaginaerum.economy_core.init.tride;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

/** Клиент -> сервер: правка правил продажи. xp = -1 — авто. */
public record SellEditPayload(int action, int idx, String target, int count, int price,
                              String currency, int xp) implements CustomPacketPayload {

    public static final int SET = 0, DELETE = 1, ADD = 2, RESET = 3;

    public static final Type<SellEditPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "sell_edit"));

    public static final StreamCodec<ByteBuf, SellEditPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.VAR_INT.encode(buf, p.action);
                ByteBufCodecs.VAR_INT.encode(buf, p.idx);
                ByteBufCodecs.STRING_UTF8.encode(buf, p.target);
                ByteBufCodecs.VAR_INT.encode(buf, p.count);
                ByteBufCodecs.VAR_INT.encode(buf, p.price);
                ByteBufCodecs.STRING_UTF8.encode(buf, p.currency);
                ByteBufCodecs.VAR_INT.encode(buf, p.xp);
            },
            buf -> new SellEditPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)));

    public static SellEditPayload set(int idx, String target, int count, int price, String currency, int xp) {
        return new SellEditPayload(SET, idx, target, count, price, currency, xp);
    }
    public static SellEditPayload delete(int idx) {
        return new SellEditPayload(DELETE, idx, "", 0, 0, "", -1);
    }
    public static SellEditPayload add(String target, int count, int price, String currency) {
        return new SellEditPayload(ADD, -1, target, count, price, currency, -1);
    }
    public static SellEditPayload reset() {
        return new SellEditPayload(RESET, -1, "", 0, 0, "", -1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}