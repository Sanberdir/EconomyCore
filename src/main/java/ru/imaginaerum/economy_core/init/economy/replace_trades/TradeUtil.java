package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class TradeUtil {

    private static final float MULT = 0.05F;

    /** Стирает все ванильные торги профессии. */
    public static void clear(VillagerTradesEvent e) {
        e.getTrades().values().forEach(List::clear);
    }

    /** Цена -> результат. */
    public static void add(VillagerTradesEvent e, int level,
                           Item in, int inCount, Item out, int outCount, int maxUses, int xp) {
        e.getTrades().get(level).add(new BasicItemListing(
                new ItemStack(in, inCount), new ItemStack(out, outCount), maxUses, xp, MULT));
    }

    /** Цена A + цена B -> результат. */
    public static void add2(VillagerTradesEvent e, int level,
                            Item in, int inCount, Item in2, int in2Count,
                            Item out, int outCount, int maxUses, int xp) {
        e.getTrades().get(level).add(new BasicItemListing(
                new ItemStack(in, inCount), new ItemStack(in2, in2Count),
                new ItemStack(out, outCount), maxUses, xp, MULT));
    }

    /** Предмет со случайным зачарованием. Цена = baseCost + уровень зачарования (макс. 64). */
    public static void addEnchanted(VillagerTradesEvent e, int level,
                                    Item payItem, int payCount,
                                    Item result, int minLvl, int maxLvl,
                                    int maxUses, int xp) {
        e.getTrades().get(level).add((trader, random) -> {
            var access = trader.level().registryAccess();
            var tag = access.registryOrThrow(Registries.ENCHANTMENT)
                    .getTag(EnchantmentTags.ON_TRADED_EQUIPMENT);
            int power = minLvl + random.nextInt(Math.max(1, maxLvl - minLvl + 1));
            ItemStack stack = EnchantmentHelper.enchantItem(
                    random, new ItemStack(result), power, access, tag);
            return new MerchantOffer(new ItemCost(payItem, payCount), stack, maxUses, xp, 0.2F);
        });
    }

    /** Книга со случайным зачарованием за фиксированную цену.
     *  payItem2 = null, если вторая цена не нужна (ваниль берёт книгу). */
    public static void addEnchantedBook(VillagerTradesEvent e, int level,
                                        Item payItem, int payCount,
                                        @Nullable Item payItem2, int pay2Count,
                                        TagKey<Enchantment> tag, int minLvl, int maxLvl,
                                        int maxUses, int xp) {
        e.getTrades().get(level).add((trader, random) -> {
            var reg = trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Optional<Holder<Enchantment>> opt = reg.getRandomElementOf(tag, random);
            if (opt.isEmpty()) return null;

            Holder<Enchantment> h = opt.get();
            int lo = Math.max(h.value().getMinLevel(), minLvl);
            int hi = Math.min(h.value().getMaxLevel(), maxLvl);
            int lvl = Mth.nextInt(random, lo, hi);

            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(h, lvl));
            Optional<ItemCost> cost2 = payItem2 == null
                    ? Optional.empty()
                    : Optional.of(new ItemCost(payItem2, pay2Count));
            return new MerchantOffer(new ItemCost(payItem, payCount), cost2, book, maxUses, xp, 0.2F);
        });
    }

    private TradeUtil() {}
}