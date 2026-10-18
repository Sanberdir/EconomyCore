package ru.imaginaerum.economy_core.init.economy.replace_trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.Optional;

public record TradeEntry(int level, Type type, ItemSpec price, Optional<ItemSpec> price2,
                         Optional<ItemSpec> result, int maxUses, int xp, Optional<Float> multiplier,
                         int minLevel, int maxLevel, TagKey<Enchantment> tag,
                         List<ResourceLocation> exclude) {

    public enum Type implements StringRepresentable {
        ITEM("item"), ENCHANTED_ITEM("enchanted_item"), ENCHANTED_BOOK("enchanted_book");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String name;
        Type(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
    public TradeEntry withPriceCount(int count) {
        return new TradeEntry(level, type, new ItemSpec(price.item(), count), price2, result,
                maxUses, xp, multiplier, minLevel, maxLevel, tag, exclude);
    }
    /** Правка из редактора. Для книг с чарами результат не меняется (передавайте Optional.empty()). */
    public TradeEntry withEdit(ItemSpec newPrice, Optional<ItemSpec> newResult, int maxUses, int xp) {
        Optional<ItemSpec> res = (type == Type.ENCHANTED_BOOK || newResult.isEmpty()) ? result : newResult;
        return new TradeEntry(level, type, newPrice, price2, res,
                maxUses, xp, multiplier, minLevel, maxLevel, tag, exclude);
    }
    /** Предмет + количество, записывается строкой "namespace:item*count". */
    public record ItemSpec(Holder<Item> item, int count) {
        public static final Codec<ItemSpec> CODEC = Codec.STRING.comapFlatMap(ItemSpec::parse, ItemSpec::format);

        private static DataResult<ItemSpec> parse(String s) {
            String idPart = s.trim();
            int cnt = 1;
            int star = idPart.indexOf('*');
            if (star >= 0) {
                try {
                    cnt = Integer.parseInt(idPart.substring(star + 1).trim());
                } catch (NumberFormatException ex) {
                    return DataResult.error(() -> "Неверное количество: " + s);
                }
                idPart = idPart.substring(0, star).trim();
            }
            if (cnt < 1) return DataResult.error(() -> "Количество должно быть >= 1: " + s);

            ResourceLocation loc = ResourceLocation.tryParse(idPart);
            if (loc == null) return DataResult.error(() -> "Неверный id: " + s);

            final int count = cnt;
            return BuiltInRegistries.ITEM.getHolder(loc)
                    .<DataResult<ItemSpec>>map(h -> DataResult.success(new ItemSpec(h, count)))
                    .orElseGet(() -> DataResult.error(() -> "Нет предмета " + loc));
        }

        private String format() {
            return item.unwrapKey().map(k -> k.location().toString()).orElse("minecraft:air") + "*" + count;
        }

        public ItemStack stack() { return new ItemStack(item.value(), count); }
        public ItemCost cost() { return new ItemCost(item.value(), count); }
    }

    public static final Codec<TradeEntry> CODEC = RecordCodecBuilder.<TradeEntry>create(i -> i.group(
            Codec.intRange(1, 5).fieldOf("level").forGetter(TradeEntry::level),
            Type.CODEC.optionalFieldOf("type", Type.ITEM).forGetter(TradeEntry::type),
            ItemSpec.CODEC.fieldOf("price").forGetter(TradeEntry::price),
            ItemSpec.CODEC.optionalFieldOf("price2").forGetter(TradeEntry::price2),
            ItemSpec.CODEC.optionalFieldOf("result").forGetter(TradeEntry::result),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_uses", 12).forGetter(TradeEntry::maxUses),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("xp", 1).forGetter(TradeEntry::xp),
            Codec.FLOAT.optionalFieldOf("multiplier").forGetter(TradeEntry::multiplier),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_level", 1).forGetter(TradeEntry::minLevel),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_level", 1).forGetter(TradeEntry::maxLevel),
            TagKey.codec(Registries.ENCHANTMENT).optionalFieldOf("tag", EnchantmentTags.TRADEABLE)
                    .forGetter(TradeEntry::tag),
            ResourceLocation.CODEC.listOf().optionalFieldOf("exclude", List.of())
                    .forGetter(TradeEntry::exclude)
    ).apply(i, TradeEntry::new)).validate(t ->
            (t.type != Type.ENCHANTED_BOOK && t.result.isEmpty())
                    ? DataResult.error(() -> "Для этого типа нужно поле result")
                    : DataResult.success(t));

    public VillagerTrades.ItemListing toListing() {
        return switch (type) {
            case ITEM -> simple();
            case ENCHANTED_ITEM -> enchantedItem();
            case ENCHANTED_BOOK -> enchantedBook();
        };
    }

    private float mult(float def) { return multiplier.orElse(def); }

    private MerchantOffer offer(ItemStack out, float mult) {
        return new MerchantOffer(price.cost(), price2.map(ItemSpec::cost), out, maxUses, xp, mult);
    }

    private VillagerTrades.ItemListing simple() {
        return (trader, random) -> offer(result.get().stack(), mult(0.05F));
    }

    private VillagerTrades.ItemListing enchantedItem() {
        return (trader, random) -> {
            var access = trader.level().registryAccess();
            var pool = access.registryOrThrow(Registries.ENCHANTMENT)
                    .getTag(EnchantmentTags.ON_TRADED_EQUIPMENT);
            int power = minLevel + random.nextInt(Math.max(1, maxLevel - minLevel + 1));
            ItemStack stack = EnchantmentHelper.enchantItem(random, result.get().stack(), power, access, pool);
            return offer(stack, mult(0.2F));
        };
    }

    private VillagerTrades.ItemListing enchantedBook() {
        return (trader, random) -> {
            var reg = trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            List<Holder<Enchantment>> pool = reg.getTag(tag)
                    .map(set -> set.stream().filter(h -> {
                        for (ResourceLocation id : exclude) {
                            if (h.is(id)) return false;
                        }
                        return true;
                    }).toList())
                    .orElse(List.of());
            if (pool.isEmpty()) return null;

            Holder<Enchantment> h = pool.get(random.nextInt(pool.size()));
            int lo = Math.max(h.value().getMinLevel(), minLevel);
            int hi = Math.min(h.value().getMaxLevel(), maxLevel);
            int lvl = Mth.nextInt(random, lo, Math.max(lo, hi));

            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(h, lvl));
            return offer(book, mult(0.2F));
        };
    }
}
