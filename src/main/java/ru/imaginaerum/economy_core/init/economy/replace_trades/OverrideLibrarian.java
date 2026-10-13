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

        TradeUtil.add(e, 1, coin, 20, Items.PAPER, 8, 16, 1);
        TradeUtil.add(e, 1, coin, 80, Items.BOOKSHELF, 1, 12, 1);
        TradeUtil.add(e, 1, coin, 60, Items.BOOK, 2, 16, 2);
        TradeUtil.add(e, 1, coin, 40, Items.WRITABLE_BOOK, 1, 12, 2);
        TradeUtil.addEnchantedBook(e, 1, coin, 500, null, 0,
                EnchantmentTags.TRADEABLE, 1, 1, 12, 3);

        TradeUtil.add(e, 2, coin, 50, Items.LANTERN, 1, 12, 5);
        TradeUtil.add(e, 2, coin, 60, Items.BOOK, 1, 12, 3);
        TradeUtil.add(e, 2, coin, 120, Items.INK_SAC, 8, 12, 5);
        TradeUtil.add(e, 2, coin, 180, Items.BOOKSHELF, 2, 12, 5);
        TradeUtil.addEnchantedBook(e, 2, coin, 450, null, 0,
                EnchantmentTags.TRADEABLE, 1, 2, 12, 8);

        TradeUtil.add(e, 3, coin, 40, Items.GLASS, 4, 12, 10);
        TradeUtil.add(e, 3, coin, 300, Items.MAP, 1, 12, 8);
        TradeUtil.add(e, 3, coin, 200, Items.WRITABLE_BOOK, 4, 12, 10);
        TradeUtil.add(e, 3, coin, 250, Items.LECTERN, 1, 12, 10);
        TradeUtil.addEnchantedBook(e, 3, coin, 450, null, 0,
                EnchantmentTags.TRADEABLE, 1, 3, 10, 14);
        TradeUtil.addEnchantedBook(e, 3, coin, 700, null, 0,
                EnchantmentTags.TRADEABLE, 2, 4, 6, 14);

        TradeUtil.add(e, 4, coin, 125, Items.CLOCK, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 200, Items.COMPASS, 1, 12, 15);
        TradeUtil.add(e, 4, coin, 320, Items.NAME_TAG, 1, 8, 12);
        TradeUtil.add(e, 4, coin, 400, Items.BOOKSHELF, 4, 12, 15);
        TradeUtil.addEnchantedBook(e, 4, coin, 700, null, 0,
                EnchantmentTags.TRADEABLE, 2, 4, 6, 20);
        TradeUtil.addEnchantedBook(e, 4, Items.EMERALD, 45, null, 0,
                EnchantmentTags.TRADEABLE, 3, 5, 4, 20);

        TradeUtil.add(e, 5, coin, 500, Items.ENCHANTING_TABLE, 1, 4, 30);
        TradeUtil.add(e, 5, coin, 50, Items.EXPERIENCE_BOTTLE, 1, 8, 30);
        TradeUtil.add(e, 5, coin, 350, Items.BOOKSHELF, 6, 8, 30);
        TradeUtil.add(e, 5, coin, 600, Items.NAME_TAG, 4, 6, 30);
        TradeUtil.addEnchantedBook(e, 5, coin, 480, null, 0,
                EnchantmentTags.TRADEABLE, 3, 5, 4, 30);
        TradeUtil.addEnchantedBook(e, 5, coin, 580, Items.EMERALD, 10,
                EnchantmentTags.TRADEABLE, 4, 5, 3, 30);
    }
}