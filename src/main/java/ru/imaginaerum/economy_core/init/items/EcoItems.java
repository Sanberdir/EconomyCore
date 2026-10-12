package ru.imaginaerum.economy_core.init.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.imaginaerum.economy_core.EconomyCore;

public class EcoItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EconomyCore.MODID);

    public static final DeferredItem<Item> GOLD_COIN = ITEMS.register("gold_coin",
            () -> new Item(new Item.Properties()));
}
