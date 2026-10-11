package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.imaginaerum.economy_core.EconomyCore;

public class EconomyMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, EconomyCore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<TraderMenu>> TRADER =
            MENUS.register("trader",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> new TraderMenu(id, inv, buf)));
}