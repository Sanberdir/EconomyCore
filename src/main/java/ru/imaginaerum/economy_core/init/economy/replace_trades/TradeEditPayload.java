package ru.imaginaerum.economy_core.init.economy.replace_trades;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

/** Клиент -> сервер: удалить торг, добавить торг или сбросить профессию к JSON из мода. */
public record TradeEditPayload(int action, String profession, int level, int idx,
                               String resultId, int resultCount,
                               String priceId, int priceCount, int maxUses, int xp)
        implements CustomPacketPayload {

    public static final int DELETE = 0, ADD = 1, RESET = 2, EDIT = 3;

    public static final Type<TradeEditPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "trade_edit"));

    public static final StreamCodec<ByteBuf, TradeEditPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.VAR_INT.encode(buf, p.action);
                ByteBufCodecs.STRING_UTF8.encode(buf, p.profession);
                ByteBufCodecs.VAR_INT.encode(buf, p.level);
                ByteBufCodecs.VAR_INT.encode(buf, p.idx);
                ByteBufCodecs.STRING_UTF8.encode(buf, p.resultId);
                ByteBufCodecs.VAR_INT.encode(buf, p.resultCount);
                ByteBufCodecs.STRING_UTF8.encode(buf, p.priceId);
                ByteBufCodecs.VAR_INT.encode(buf, p.priceCount);
                ByteBufCodecs.VAR_INT.encode(buf, p.maxUses);
                ByteBufCodecs.VAR_INT.encode(buf, p.xp);
            },
            buf -> new TradeEditPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)));

    public static TradeEditPayload delete(String prof, int level, int idx) {
        return new TradeEditPayload(DELETE, prof, level, idx, "", 0, "", 0, 0, 0);
    }
    public static TradeEditPayload edit(String prof, int level, int idx, String resultId, int resultCount,
                                        String priceId, int priceCount, int maxUses, int xp) {
        return new TradeEditPayload(EDIT, prof, level, idx, resultId, resultCount,
                priceId, priceCount, maxUses, xp);
    }
    public static TradeEditPayload reset(String prof) {
        return new TradeEditPayload(RESET, prof, 0, 0, "", 0, "", 0, 0, 0);
    }

    public static TradeEditPayload add(String prof, int level, String resultId, int resultCount,
                                       String priceId, int priceCount, int maxUses, int xp) {
        return new TradeEditPayload(ADD, prof, level, 0, resultId, resultCount,
                priceId, priceCount, maxUses, xp);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}