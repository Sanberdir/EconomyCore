package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** Строка панели. sold = true: уже проданное (можно выкупить, id), иначе надетое (key). amount = 0: не покупается. */
public record EquipLine(boolean sold, int id, String key, ItemStack stack, String currency, int amount) {

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipLine> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EquipLine::sold,
            ByteBufCodecs.VAR_INT, EquipLine::id,
            ByteBufCodecs.STRING_UTF8, EquipLine::key,
            ItemStack.OPTIONAL_STREAM_CODEC, EquipLine::stack,
            ByteBufCodecs.STRING_UTF8, EquipLine::currency,
            ByteBufCodecs.VAR_INT, EquipLine::amount,
            EquipLine::new);
}