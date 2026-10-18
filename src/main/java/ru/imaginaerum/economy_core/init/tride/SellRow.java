package ru.imaginaerum.economy_core.init.tride;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** kind: 0 — предмет, 1 — тег (target без #), 2 — любой предмет. xp = -1 — авто. */
public record SellRow(int idx, int kind, String target, int count, int price,
                      String currency, boolean deny, boolean complex, int xp) {

    public static final StreamCodec<ByteBuf, SellRow> CODEC = StreamCodec.of(
            (buf, r) -> {
                ByteBufCodecs.VAR_INT.encode(buf, r.idx);
                ByteBufCodecs.VAR_INT.encode(buf, r.kind);
                ByteBufCodecs.STRING_UTF8.encode(buf, r.target);
                ByteBufCodecs.VAR_INT.encode(buf, r.count);
                ByteBufCodecs.VAR_INT.encode(buf, r.price);
                ByteBufCodecs.STRING_UTF8.encode(buf, r.currency);
                ByteBufCodecs.BOOL.encode(buf, r.deny);
                ByteBufCodecs.BOOL.encode(buf, r.complex);
                ByteBufCodecs.VAR_INT.encode(buf, r.xp);
            },
            buf -> new SellRow(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)));

    /** "minecraft:iron_ingot", "#minecraft:swords" или "*". */
    public String targetId() {
        return kind == 1 ? "#" + target : target;
    }

    public SellRow with(int count, int price, int xp) {
        return new SellRow(idx, kind, target, count, price, currency, deny, complex, xp);
    }

    public SellRow withTarget(String id) {
        int k = id.equals("*") ? 2 : id.startsWith("#") ? 1 : 0;
        String t = k == 1 ? id.substring(1) : id;
        return new SellRow(idx, k, t, count, price, currency, deny, complex, xp);
    }

    public SellRow withCurrency(String id) {
        return new SellRow(idx, kind, target, count, price, id, deny, complex, xp);
    }
}