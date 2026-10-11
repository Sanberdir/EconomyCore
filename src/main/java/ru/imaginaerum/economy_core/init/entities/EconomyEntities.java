package ru.imaginaerum.economy_core.init.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;

public class EconomyEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EconomyCore.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EconomyTrader>> TRADER =
            ENTITIES.register("trader", () -> EntityType.Builder
                    .of(EconomyTrader::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("trader"));
}