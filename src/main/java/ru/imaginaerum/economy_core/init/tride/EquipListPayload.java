package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

import java.util.List;

/** Сервер -> клиент: надетое и проданное в этом окне. */
public record EquipListPayload(List<EquipLine> lines) implements CustomPacketPayload {

    public static final Type<EquipListPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "equip_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipListPayload> CODEC =
            EquipLine.CODEC.apply(ByteBufCodecs.list())
                    .map(EquipListPayload::new, EquipListPayload::lines);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}