package ru.imaginaerum.economy_core.init.economy;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import ru.imaginaerum.economy_core.EconomyCore;

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
}