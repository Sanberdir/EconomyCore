package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.trading.MerchantOffers;
import ru.imaginaerum.economy_core.EconomyCore;

public record SyncOffersPayload(int containerId, MerchantOffers offers) implements CustomPacketPayload {

    public static final Type<SyncOffersPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "sync_offers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncOffersPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncOffersPayload::containerId,
            MerchantOffers.STREAM_CODEC, SyncOffersPayload::offers,
            SyncOffersPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}