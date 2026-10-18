package ru.imaginaerum.economy_core.init.tride;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.imaginaerum.economy_core.EconomyCore;

/** Клиент -> сервер: список надетого, продажа слота или выкуп. */
public record EquipActionPayload(int action, String key, int id) implements CustomPacketPayload {

    public static final int LIST = 0, SELL = 1, BUYBACK = 2, COMMIT = 3;

    public static EquipActionPayload commit() { return new EquipActionPayload(COMMIT, "", 0); }

    public static final Type<EquipActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "equip_action"));

    public static final StreamCodec<ByteBuf, EquipActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EquipActionPayload::action,
            ByteBufCodecs.STRING_UTF8, EquipActionPayload::key,
            ByteBufCodecs.VAR_INT, EquipActionPayload::id,
            EquipActionPayload::new);

    public static EquipActionPayload list() { return new EquipActionPayload(LIST, "", 0); }
    public static EquipActionPayload sell(String key) { return new EquipActionPayload(SELL, key, 0); }
    public static EquipActionPayload buyback(int id) { return new EquipActionPayload(BUYBACK, "", id); }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}