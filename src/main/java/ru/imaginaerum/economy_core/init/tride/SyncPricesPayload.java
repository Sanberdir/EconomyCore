package ru.imaginaerum.economy_core.init.tride;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

import java.util.List;

public record SyncPricesPayload(List<String> entries) implements CustomPacketPayload {

    public static final Type<SyncPricesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "sync_prices"));

    public static final StreamCodec<ByteBuf, SyncPricesPayload> CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())
                    .map(SyncPricesPayload::new, SyncPricesPayload::entries);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}