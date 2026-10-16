package ru.imaginaerum.economy_core.init.economy;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import ru.imaginaerum.economy_core.EconomyCore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EconomyAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EconomyCore.MODID);

    /** Изумруды на счёте игрока: сохраняется, синхронизируется с клиентом, переживает смерть. */
    public static final Supplier<AttachmentType<Integer>> BALANCE = ATTACHMENTS.register("balance",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .sync(ByteBufCodecs.VAR_INT)
                    .copyOnDeath()
                    .build());
    public static final Supplier<AttachmentType<Integer>> COIN_BALANCE = ATTACHMENTS.register("coin_balance",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .sync(ByteBufCodecs.VAR_INT)
                    .copyOnDeath()
                    .build());
// импорты: com.mojang.serialization.Codec, net.minecraft.world.item.trading.MerchantOffers, java.util.*

    /** Запомненные торги жителя: id профессии -> торги. Переживает смену профессии и перезапуск. */
    public static final Supplier<AttachmentType<Map<String, MerchantOffers>>> TRADE_MEMORY =
            ATTACHMENTS.register("trade_memory",
                    () -> AttachmentType.<Map<String, MerchantOffers>>builder(() -> new HashMap<>())
                            .serialize(Codec.unboundedMap(Codec.STRING, MerchantOffers.CODEC))
                            .build());

    /** Последняя замеченная профессия (для определения смены). */
    public static final Supplier<AttachmentType<String>> LAST_PROFESSION =
            ATTACHMENTS.register("last_profession",
                    () -> AttachmentType.builder(() -> "")
                            .serialize(Codec.STRING)
                            .build());
}