package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public final class TraderPrices {

    /** За {@code count} предметов торгаш даёт {@code emeralds} изумрудов. */
    public record SellEntry(Item item, int count, int emeralds) {}

    // Чтобы добавить вещь, допиши сюда одну строку.
    private static final List<SellEntry> SELL = List.of(
            new SellEntry(Items.COPPER_INGOT, 6, 1),
            new SellEntry(Items.IRON_INGOT, 3, 1),
            new SellEntry(Items.GOLD_INGOT, 1, 2),
            new SellEntry(Items.NETHERITE_INGOT, 1, 40)
    );

    public static List<SellEntry> entries() {
        return SELL;
    }

    private TraderPrices() {}
}