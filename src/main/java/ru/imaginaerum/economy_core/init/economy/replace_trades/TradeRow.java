package ru.imaginaerum.economy_core.init.economy.replace_trades;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TradeRow(String profession, int level, int idx, String resultId, int resultCount,
                       String priceId, int priceCount, int defaultCount, boolean enchanted,
                       int maxUses, int xp, boolean resultEditable) {
    public static final int MAX_PER_LEVEL = 8;
    public static final StreamCodec<ByteBuf, TradeRow> CODEC = StreamCodec.of(
            (buf, r) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, r.profession);
                ByteBufCodecs.VAR_INT.encode(buf, r.level);
                ByteBufCodecs.VAR_INT.encode(buf, r.idx);
                ByteBufCodecs.STRING_UTF8.encode(buf, r.resultId);
                ByteBufCodecs.VAR_INT.encode(buf, r.resultCount);
                ByteBufCodecs.STRING_UTF8.encode(buf, r.priceId);
                ByteBufCodecs.VAR_INT.encode(buf, r.priceCount);
                ByteBufCodecs.VAR_INT.encode(buf, r.defaultCount);
                ByteBufCodecs.BOOL.encode(buf, r.enchanted);
                ByteBufCodecs.VAR_INT.encode(buf, r.maxUses);
                ByteBufCodecs.VAR_INT.encode(buf, r.xp);
                ByteBufCodecs.BOOL.encode(buf, r.resultEditable);
            },
            buf -> new TradeRow(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    public TradeRow withPrice(int c) {
        return new TradeRow(profession, level, idx, resultId, resultCount, priceId, c, defaultCount,
                enchanted, maxUses, xp, resultEditable);
    }

    public TradeRow withResultId(String id) {
        return new TradeRow(profession, level, idx, id, resultCount, priceId, priceCount, defaultCount,
                enchanted, maxUses, xp, resultEditable);
    }

    public TradeRow withPriceId(String id) {
        return new TradeRow(profession, level, idx, resultId, resultCount, id, priceCount, defaultCount,
                enchanted, maxUses, xp, resultEditable);
    }

    public TradeRow with(int resultCount, int priceCount, int maxUses, int xp) {
        return new TradeRow(profession, level, idx, resultId, resultCount, priceId, priceCount, defaultCount,
                enchanted, maxUses, xp, resultEditable);
    }
}