package ru.imaginaerum.economy_core.init.economy;


import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

public record WithdrawPayload() implements CustomPacketPayload {

    public static final Type<WithdrawPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "withdraw"));
    public static final StreamCodec<ByteBuf, WithdrawPayload> CODEC =
            StreamCodec.unit(new WithdrawPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
