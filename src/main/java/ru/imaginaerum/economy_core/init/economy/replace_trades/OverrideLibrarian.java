package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class OverrideLibrarian {

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.LIBRARIAN) return;
        TradeUtil.clear(e);

        // ---------- Уровень 1: Новичок ----------
        TradeUtil.add(e, 1, Items.PAPER, 24, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.SUGAR_CANE, 30, Items.EMERALD, 1, 16, 2);
        TradeUtil.add(e, 1, Items.EMERALD, 9, Items.BOOKSHELF, 1, 12, 1);
        TradeUtil.add(e, 1, Items.EMERALD, 1, Items.PAPER, 8, 16, 1);
        // Дешёвая книга чар I уровня: 6 изумрудов + книга
        TradeUtil.addEnchantedBook(e, 1, Items.EMERALD, 6, Items.BOOK, 1,
                EnchantmentTags.TRADEABLE, 1, 1, 12, 3);

        // ---------- Уровень 2: Ученик ----------
        TradeUtil.add(e, 2, Items.BOOK, 4, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.LEATHER, 6, Items.EMERALD, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 1, Items.LANTERN, 1, 12, 5);
        TradeUtil.add(e, 2, Items.EMERALD, 2, Items.BOOK, 2, 12, 3);
        TradeUtil.add2(e, 2, Items.EMERALD, 5, Items.BOOK, 3, Items.BOOKSHELF, 1, 12, 5);
        // Книга чар до II уровня: 12 изумрудов + книга
        TradeUtil.addEnchantedBook(e, 2, Items.EMERALD, 12, Items.BOOK, 1,
                EnchantmentTags.TRADEABLE, 1, 2, 12, 8);

        // ---------- Уровень 3: Подмастерье ----------
        TradeUtil.add(e, 3, Items.INK_SAC, 5, Items.EMERALD, 1, 12, 10);
        TradeUtil.add(e, 3, Items.GLOW_INK_SAC, 3, Items.EMERALD, 2, 12, 10);
        TradeUtil.add(e, 3, Items.EMERALD, 1, Items.GLASS, 4, 12, 10);
        TradeUtil.add2(e, 3, Items.EMERALD, 2, Items.PAPER, 6, Items.MAP, 1, 12, 8);
        // Книга чар до III уровня: 22 изумруда + книга
        TradeUtil.addEnchantedBook(e, 3, Items.EMERALD, 22, Items.BOOK, 1,
                EnchantmentTags.TRADEABLE, 1, 3, 10, 14);
        // Сильная книга без книги в оплату, дороже: 30 изумрудов
        TradeUtil.addEnchantedBook(e, 3, Items.EMERALD, 30, null, 0,
                EnchantmentTags.TRADEABLE, 2, 4, 6, 14);

        // ---------- Уровень 4: Эксперт ----------
        TradeUtil.add(e, 4, Items.WRITABLE_BOOK, 2, Items.EMERALD, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 5, Items.CLOCK, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 4, Items.COMPASS, 1, 12, 15);
        TradeUtil.add(e, 4, Items.EMERALD, 3, Items.NAME_TAG, 1, 8, 12);
        // Книга чар до IV уровня: 36 изумрудов + книга
        TradeUtil.addEnchantedBook(e, 4, Items.EMERALD, 36, Items.BOOK, 1,
                EnchantmentTags.TRADEABLE, 2, 4, 6, 20);
        // Книга за алмазы
        TradeUtil.addEnchantedBook(e, 4, Items.DIAMOND, 4, Items.BOOK, 1,
                EnchantmentTags.TRADEABLE, 3, 5, 4, 20);

        // ---------- Уровень 5: Мастер ----------
        TradeUtil.add(e, 5, Items.EMERALD, 20, Items.NAME_TAG, 1, 6, 30);
        TradeUtil.add2(e, 5, Items.EMERALD, 12, Items.DIAMOND, 1, Items.ENCHANTING_TABLE, 1, 4, 30);
        TradeUtil.add(e, 5, Items.EMERALD, 6, Items.EXPERIENCE_BOTTLE, 3, 8, 30);
        // Топовая книга любого уровня: 55 изумрудов + книга
        TradeUtil.addEnchantedBook(e, 5, Items.EMERALD, 55, Items.BOOK, 1,
                EnchantmentTags.TRADEABLE, 3, 5, 4, 30);
        // Редкая книга за алмазы и изумруды (вторая цена: алмазные блоки не нужны, просто алмазы)
        TradeUtil.addEnchantedBook(e, 5, Items.DIAMOND, 8, Items.EMERALD, 20,
                EnchantmentTags.TRADEABLE, 4, 5, 3, 30);
    }
}