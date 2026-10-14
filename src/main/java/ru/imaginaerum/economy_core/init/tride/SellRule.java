package ru.imaginaerum.economy_core.init.tride;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================ ИНСТРУКЦИЯ: ЦЕНЫ ПРОДАЖИ ТОРГОВЦУ ============================
 *
 * Где лежат файлы:  src/main/resources/data/<любой_namespace>/trader_prices/<имя>.json
 * (то же самое работает из датапаков). Файлов может быть сколько угодно, правила всех
 * файлов объединяются. Применить изменения без перезапуска: команда /reload.
 *
 * Формат файла:
 *   { "entries": [ { правило }, { правило }, ... ] }
 *
 * ---------- ЧТО ПРОДАЁМ (нужно указать ОДНО из трёх) ----------
 *   "item": "minecraft:iron_ingot"   конкретный предмет
 *   "tag":  "minecraft:swords"       все предметы тега (без #). Теги модов тоже: "c:ingots/iron"
 *   "all":  true                     вообще любой предмет (запасной вариант, ставь низкий priority)
 *
 * ---------- ДОПОЛНИТЕЛЬНЫЙ ФИЛЬТР ----------
 *   "potion": "minecraft:healing"    только для зелий / взрывных / туманных / стрел с эффектом:
 *                                    подходит предмет с именно этим зельем.
 *                                    Используй вместе с "item": "minecraft:potion" и т.п.
 *
 * ---------- ЦЕНА ----------
 *   "count":    1      сколько штук в одной партии (по умолчанию 1)
 *   "emeralds": 1      сколько изумрудов за партию (по умолчанию 1)
 *   "xp":       3      опыт торговца за партию. Если не указан, то половина цены (минимум 1)
 *
 * ---------- ЧАРЫ (для инструментов, брони, оружия и зачарованных книг) ----------
 *   "enchant_per_level": 2     надбавка к цене за каждый уровень каждого зачарования
 *                              (Sharpness V = +10, Unbreaking III = +6 и т.д.)
 *   "enchants": { "minecraft:mending": 30, "minecraft:sharpness": 5 }
 *                              своя цена за уровень для конкретных чар
 *                              (перекрывает enchant_per_level для этих чар)
 *
 * ---------- ПРОЧНОСТЬ ----------
 *   "scale_durability": true   цена умножается на долю оставшейся прочности.
 *                              Почти сломанная кирка стоит дёшево.
 *                              Действует на всю цену, вместе с надбавкой за чары.
 *
 * ---------- ПРИОРИТЕТ И ЗАПРЕТ ----------
 *   "priority": 0      у предмета подходит несколько правил? Побеждает то, у которого priority больше
 *                      (по умолчанию 0). При равенстве побеждает более точное правило:
 *                      потион-фильтр, потом item, потом tag, потом all.
 *                      Поэтому правило на diamond_sword автоматически сильнее тега "swords".
 *   "deny": true       предмет, подошедший под это правило, продать нельзя
 *                      (например, запретить что-то из тега, который иначе продаётся).
 *
 * ---------- ИТОГОВАЯ ЦЕНА ПАРТИИ ----------
 *   (emeralds + сумма надбавок за чары) * доля прочности (если scale_durability)
 *   Если вышло 0, то предмет не продаётся.
 *
 * Не продаются никогда (жёстко в коде): изумруд и изумрудный блок.
 * Предмета нет в сборке (мод не установлен)? Правило пропускается, ошибка пишется в лог.
 * Продаётся всё из основного инвентаря и хотбара (36 слотов), броня на игроке и левая рука не учитываются.
 */
public record SellRule(
        Optional<Holder<Item>> item,
        Optional<TagKey<Item>> tag,
        boolean all,
        Optional<Holder<Potion>> potion,
        int count,
        int emeralds,
        Optional<Integer> xp,
        int enchantPerLevel,
        Map<ResourceLocation, Integer> enchants,
        boolean scaleDurability,
        int priority,
        boolean deny,
        Optional<Holder<Item>> currency) {

    public static final Codec<SellRule> CODEC = RecordCodecBuilder.<SellRule>create(i -> i.group(
            BuiltInRegistries.ITEM.holderByNameCodec().optionalFieldOf("item").forGetter(SellRule::item),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(SellRule::tag),
            Codec.BOOL.optionalFieldOf("all", false).forGetter(SellRule::all),
            BuiltInRegistries.POTION.holderByNameCodec().optionalFieldOf("potion").forGetter(SellRule::potion),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SellRule::count),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("emeralds", 1).forGetter(SellRule::emeralds),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("xp").forGetter(SellRule::xp),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("enchant_per_level", 0).forGetter(SellRule::enchantPerLevel),
            Codec.unboundedMap(ResourceLocation.CODEC, ExtraCodecs.NON_NEGATIVE_INT)
                    .optionalFieldOf("enchants", Map.of()).forGetter(SellRule::enchants),
            Codec.BOOL.optionalFieldOf("scale_durability", true).forGetter(SellRule::scaleDurability),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(SellRule::priority),
            Codec.BOOL.optionalFieldOf("deny", false).forGetter(SellRule::deny),
            BuiltInRegistries.ITEM.holderByNameCodec().optionalFieldOf("currency").forGetter(SellRule::currency)
    ).apply(i, SellRule::new)).validate(r ->
            (r.item.isPresent() || r.tag.isPresent() || r.all)
                    ? DataResult.success(r)
                    : DataResult.error(() -> "Нужно указать одно из полей: item, tag или all"));

    /** Чем точнее правило, тем выше число (при равном priority побеждает более точное). */
    public int specificity() {
        if (potion.isPresent()) return 3;
        if (item.isPresent()) return 2;
        if (tag.isPresent()) return 1;
        return 0;
    }

    public boolean matches(ItemStack s) {
        boolean byId = all
                || item.map(h -> s.is(h)).orElse(false)
                || tag.map(t -> s.is(t)).orElse(false);
        if (!byId) return false;
        if (potion.isPresent()) {
            PotionContents pc = s.get(DataComponents.POTION_CONTENTS);
            if (pc == null) return false;
            Potion wanted = potion.get().value();
            return pc.potion().map(h -> h.value() == wanted).orElse(false);
        }
        return true;
    }

    /** Цена партии для конкретного стека, null если выходит 0. */
    @Nullable
    public TraderPrices.Quote price(ItemStack s) {
        int bonus = 0;
        for (DataComponentType<ItemEnchantments> type
                : List.of(DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS)) {
            ItemEnchantments en = s.getOrDefault(type, ItemEnchantments.EMPTY);
            for (var e : en.entrySet()) {
                int perLevel = e.getKey().unwrapKey()
                        .map(k -> enchants.getOrDefault(k.location(), enchantPerLevel))
                        .orElse(enchantPerLevel);
                bonus += perLevel * e.getIntValue();
            }
        }
        double sum = emeralds + bonus;
        if (scaleDurability && s.isDamageableItem()) {
            sum *= (double) (s.getMaxDamage() - s.getDamageValue()) / s.getMaxDamage();
        }
        int result = (int) Math.round(sum);
        if (result <= 0) return null;
        Item cur = currency.map(Holder::value).orElse(Items.EMERALD);
        return new TraderPrices.Quote(count, result, xp.orElse(Math.max(1, result / 2)), cur);
    }
}