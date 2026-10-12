package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.items.EcoItems;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideLibrarian {
    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.LIBRARIAN) return;
        TradeUtil.clear(e);
        Item coin = EcoItems.GOLD_COIN.get();

        // 1
        TradeUtil.add(e, 1, coin, 20, Items.PAPER, 8, 16, 1);
        TradeUtil.add(e, 1, coin, 150, Items.BOOKSHELF, 1, 12, 1);
        TradeUtil.addEnchantedBook(e, 1, coin, 100, null, 0,
                EnchantmentTags.TRADEABLE, 1, 1, 12, 3);

        // 2
        TradeUtil.add(e, 2, coin, 50, Items.LANTERN, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 60, Items.BOOK, 2, 12, 3);
        TradeUtil.addEnchantedBook(e, 2, coin, 250, null, 0,
                EnchantmentTags.TRADEABLE, 1, 2, 12, 8);

        // 3
        TradeUtil.add(e, 3, coin, 30, Items.GLASS, 4, 12, 10);
        TradeUtil.add(e, 3, coin, 100, Items.MAP, 1, 12, 8);
        TradeUtil.addEnchantedBook(e, 3, coin, 450, null, 0,
                EnchantmentTags.TRADEABLE, 1, 3, 10, 14);
        TradeUtil.addEnchantedBook(e, 3, coin, 700, null, 0,
                EnchantmentTags.TRADEABLE, 2, 4, 6, 14);

        // 4
        TradeUtil.add(e, 4, coin, 125, Items.CLOCK, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 100, Items.COMPASS, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 80, Items.NAME_TAG, 1, 8, 12);
        TradeUtil.addEnchantedBook(e, 4, coin, 1000, null, 0,
                EnchantmentTags.TRADEABLE, 2, 4, 6, 20);
        TradeUtil.addEnchantedBook(e, 4, Items.EMERALD, 45, null, 0,
                EnchantmentTags.TRADEABLE, 3, 5, 4, 20);

        // 5: дорого, изумруды
        TradeUtil.add(e, 5, Items.EMERALD, 24, Items.ENCHANTING_TABLE, 1, 4, 30);
        TradeUtil.add(e, 5, coin, 300, Items.EXPERIENCE_BOTTLE, 3, 8, 30);
        TradeUtil.addEnchantedBook(e, 5, Items.EMERALD, 60, null, 0,
                EnchantmentTags.TRADEABLE, 3, 5, 4, 30);
        TradeUtil.addEnchantedBook(e, 5, Items.EMERALD, 80, coin, 500,
                EnchantmentTags.TRADEABLE, 4, 5, 3, 30);
    }
}