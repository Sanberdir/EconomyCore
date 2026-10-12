package ru.imaginaerum.economy_core.init.tride;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import ru.imaginaerum.economy_core.init.economy.EconomyBank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Хранит правила продажи. Формат JSON описан в {@link SellRule}. */
public final class TraderPrices {

    private static final Logger LOG = LogUtils.getLogger();

    /** Цена одной партии: отдать count штук, получить emeralds изумрудов, торговец получает xp. */
    public record Quote(int count, int amount, int xp, Item currency) {}

    /** Строка вкладки «Продать»: stack (с размером партии), слот-представитель, цена, сколько всего есть. */
    public record SellLine(ItemStack stack, int slot, Quote quote, int have) {}

    /** Валюта: не продаётся никогда. */
    private static final Set<Item> CURRENCY = Set.of(Items.EMERALD, Items.EMERALD_BLOCK);

    private static volatile List<SellRule> RULES = List.of();
    private static volatile List<String> RAW = List.of();

    public static boolean isCurrency(Item item) {
        return EconomyBank.isCurrency(item);
    }

    /** Исходные JSON-записи (для отправки клиенту). */
    public static List<String> raw() {
        return RAW;
    }

    /** Разбирает записи и заменяет текущие правила. Вызывается на сервере (reload) и на клиенте (пакет). */
    public static void load(List<String> raw) {
        List<SellRule> list = new ArrayList<>();
        for (String s : raw) {
            try {
                JsonElement el = JsonParser.parseString(s);
                SellRule.CODEC.parse(JsonOps.INSTANCE, el)
                        .resultOrPartial(err -> LOG.error("[TraderPrices] Пропущено правило {} : {}", s, err))
                        .ifPresent(list::add);
            } catch (Exception ex) {
                LOG.error("[TraderPrices] Некорректный JSON: {}", s, ex);
            }
        }
        list.sort((a, b) -> a.priority() != b.priority()
                ? Integer.compare(b.priority(), a.priority())
                : Integer.compare(b.specificity(), a.specificity()));
        RULES = List.copyOf(list);
        RAW = List.copyOf(raw);
        LOG.info("[TraderPrices] Загружено правил продажи: {}", RULES.size());
    }

    /** Цена партии для стека или null, если продать нельзя. */
    @Nullable
    public static Quote quote(ItemStack s) {
        if (s.isEmpty() || isCurrency(s.getItem())) return null;
        for (SellRule r : RULES) {
            if (!r.matches(s)) continue;
            return r.deny() ? null : r.price(s);   // первое подошедшее правило решает
        }
        return null;
    }

    /** Список для вкладки «Продать»: одинаковые стеки (предмет + компоненты) объединяются в одну строку. */
    public static List<SellLine> forPlayer(Player p) {
        List<ItemStack> items = p.getInventory().items;
        boolean[] used = new boolean[items.size()];
        List<SellLine> out = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty() || used[i]) continue;
            Quote q = quote(s);
            if (q == null) continue;

            int have = 0;
            for (int j = i; j < items.size(); j++) {
                ItemStack t = items.get(j);
                if (!used[j] && ItemStack.isSameItemSameComponents(s, t)) {
                    have += t.getCount();
                    used[j] = true;
                }
            }
            out.add(new SellLine(s.copyWithCount(q.count()), i, q, have));
        }
        return out;
    }

    private TraderPrices() {}
}