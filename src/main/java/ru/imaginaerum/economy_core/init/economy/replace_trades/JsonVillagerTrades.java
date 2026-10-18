package ru.imaginaerum.economy_core.init.economy.replace_trades;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.slf4j.Logger;
import ru.imaginaerum.economy_core.EconomyCore;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Торги жителей. Источник по умолчанию: data/<ns>/villager_trades/<профессия>.json.
 * Если есть config/economy_core/villager_trades/<профессия>.json (создаётся меню /economycore sell),
 * он полностью заменяет список этой профессии.
 */
@EventBusSubscriber(modid = EconomyCore.MODID)
public final class JsonVillagerTrades extends SimpleJsonResourceReloadListener {

    public static final int MAX_COUNT = 9_999_999;

    private static final Logger LOG = LogUtils.getLogger();
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR =
            FMLPaths.CONFIGDIR.get().resolve("economy_core").resolve("villager_trades");
    private static final Object LOCK = new Object();

    /** Из датапака/мода. */
    private static final Map<String, List<TradeEntry>> DATA = new HashMap<>();
    /** Из config: перекрывает DATA. */
    private static final Map<String, List<TradeEntry>> EDITED = new HashMap<>();

    public JsonVillagerTrades() {
        super(new Gson(), "villager_trades");
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent e) {
        e.addListener(new JsonVillagerTrades());
    }

    // ---------- Загрузка ----------

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager rm, ProfilerFiller profiler) {
        synchronized (LOCK) {
            DATA.clear();
            for (var f : new TreeMap<>(files).entrySet()) {
                List<TradeEntry> parsed = parse(f.getValue(), f.getKey().toString());
                if (parsed == null) continue;
                DATA.computeIfAbsent(f.getKey().getPath(), k -> new ArrayList<>()).addAll(parsed);
            }
            EDITED.clear();
            loadEdited();
            LOG.info("[Trades] Из мода: {}, из config: {}", DATA.keySet(), EDITED.keySet());
        }
    }

    @Nullable
    private static List<TradeEntry> parse(JsonElement root, String label) {
        if (!root.isJsonObject() || !root.getAsJsonObject().has("trades")
                || !root.getAsJsonObject().get("trades").isJsonArray()) {
            LOG.error("[Trades] {}: нет массива trades", label);
            return null;
        }
        List<TradeEntry> out = new ArrayList<>();
        for (JsonElement el : root.getAsJsonObject().getAsJsonArray("trades")) {
            TradeEntry.CODEC.parse(JsonOps.INSTANCE, el)
                    .resultOrPartial(err -> LOG.error("[Trades] {}: пропущено {} : {}", label, el, err))
                    .ifPresent(out::add);
        }
        return out;
    }

    private static void loadEdited() {
        if (!Files.isDirectory(DIR)) return;
        try (var s = Files.list(DIR)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                String n = p.getFileName().toString();
                if (!n.endsWith(".json")) continue;
                try (Reader r = Files.newBufferedReader(p)) {
                    List<TradeEntry> parsed = parse(JsonParser.parseReader(r), p.toString());
                    if (parsed != null) EDITED.put(n.substring(0, n.length() - 5), parsed);
                } catch (Exception ex) {
                    LOG.error("[Trades] Ошибка чтения {}", p, ex);
                }
            }
        } catch (IOException ex) {
            LOG.error("[Trades] Не удалось прочитать {}", DIR, ex);
        }
    }

    private static void save(String prof) {
        JsonArray arr = new JsonArray();
        for (TradeEntry t : EDITED.get(prof)) {
            TradeEntry.CODEC.encodeStart(JsonOps.INSTANCE, t)
                    .resultOrPartial(err -> LOG.error("[Trades] {}: не записано {} : {}", prof, t, err))
                    .ifPresent(arr::add);
        }
        JsonObject o = new JsonObject();
        o.add("trades", arr);
        try {
            Files.createDirectories(DIR);
            Files.writeString(DIR.resolve(prof + ".json"), PRETTY.toJson(o));
        } catch (IOException ex) {
            LOG.error("[Trades] Не удалось сохранить {}", prof, ex);
        }
    }

    // ---------- Доступ ----------

    @Nullable
    private static List<TradeEntry> effective(String prof) {
        List<TradeEntry> l = EDITED.get(prof);
        return l != null ? l : DATA.get(prof);
    }

    /** Список для правки: при первой правке копируется из DATA. */
    @Nullable
    private static List<TradeEntry> mutable(String prof) {
        List<TradeEntry> l = EDITED.get(prof);
        if (l == null) {
            List<TradeEntry> base = DATA.get(prof);
            if (base == null) return null;
            l = new ArrayList<>(base);
            EDITED.put(prof, l);
        }
        return l;
    }

    /** Позиция в списке торга №idx внутри уровня level, либо -1. */
    private static int position(List<TradeEntry> list, int level, int idx) {
        int n = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).level() != level) continue;
            if (n++ == idx) return i;
        }
        return -1;
    }

    private static int countAt(List<TradeEntry> list, int level) {
        int n = 0;
        for (TradeEntry t : list) if (t.level() == level) n++;
        return n;
    }

    @Nullable
    private static Holder<Item> item(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return null;
        return BuiltInRegistries.ITEM.getHolder(loc).filter(h -> h.value() != Items.AIR).orElse(null);
    }

    // ---------- Правки (вызываются на сервере) ----------

    public static boolean setPrice(String prof, int level, int idx, int count) {
        if (count < 1 || count > MAX_COUNT) return false;
        synchronized (LOCK) {
            List<TradeEntry> cur = effective(prof);
            if (cur == null || position(cur, level, idx) < 0) return false;
            List<TradeEntry> l = mutable(prof);
            int pos = position(l, level, idx);
            l.set(pos, l.get(pos).withPriceCount(count));
            save(prof);
            return true;
        }
    }
    /** Правка всех полей торга из редактора. */
    public static boolean update(String prof, int level, int idx, String resultId, int resultCount,
                                 String priceId, int priceCount, int maxUses, int xp) {
        if (resultCount < 1 || resultCount > MAX_COUNT || priceCount < 1 || priceCount > MAX_COUNT) return false;
        if (maxUses < 1 || maxUses > 9999 || xp < 0 || xp > 9999) return false;
        Holder<Item> price = item(priceId);
        if (price == null) return false;

        synchronized (LOCK) {
            List<TradeEntry> cur = effective(prof);
            if (cur == null) return false;
            int p0 = position(cur, level, idx);
            if (p0 < 0) return false;

            // всё проверяем до mutable(), чтобы не создавать копию зря
            Optional<TradeEntry.ItemSpec> res = Optional.empty();
            if (cur.get(p0).type() != TradeEntry.Type.ENCHANTED_BOOK) {
                Holder<Item> r = item(resultId);
                if (r == null) return false;
                res = Optional.of(new TradeEntry.ItemSpec(r, resultCount));
            }

            List<TradeEntry> l = mutable(prof);
            int pos = position(l, level, idx);
            l.set(pos, l.get(pos).withEdit(new TradeEntry.ItemSpec(price, priceCount), res, maxUses, xp));
            save(prof);
            return true;
        }
    }
    public static boolean delete(String prof, int level, int idx) {
        synchronized (LOCK) {
            List<TradeEntry> cur = effective(prof);
            if (cur == null || position(cur, level, idx) < 0) return false;
            List<TradeEntry> l = mutable(prof);
            l.remove(position(l, level, idx));
            save(prof);
            return true;
        }
    }

    public static boolean add(String prof, int level, String resultId, int resultCount,
                              String priceId, int priceCount, int maxUses, int xp) {
        if (level < 1 || level > 5) return false;
        if (resultCount < 1 || resultCount > MAX_COUNT || priceCount < 1 || priceCount > MAX_COUNT) return false;
        if (maxUses < 1 || maxUses > 9999 || xp < 0 || xp > 9999) return false;
        Holder<Item> result = item(resultId);
        Holder<Item> price = item(priceId);
        if (result == null || price == null) return false;

        synchronized (LOCK) {
            List<TradeEntry> cur = effective(prof);
            if (cur == null || countAt(cur, level) >= TradeRow.MAX_PER_LEVEL) return false;
            List<TradeEntry> l = mutable(prof);
            l.add(new TradeEntry(level, TradeEntry.Type.ITEM,
                    new TradeEntry.ItemSpec(price, priceCount), Optional.empty(),
                    Optional.of(new TradeEntry.ItemSpec(result, resultCount)),
                    maxUses, xp, Optional.empty(), 1, 1, EnchantmentTags.TRADEABLE, List.of()));
            save(prof);
            return true;
        }
    }

    /** Удаляет правки config и возвращает торги из JSON мода. */
    public static boolean reset(String prof) {
        synchronized (LOCK) {
            if (!DATA.containsKey(prof) && !EDITED.containsKey(prof)) return false;
            EDITED.remove(prof);
            try {
                Files.deleteIfExists(DIR.resolve(prof + ".json"));
            } catch (IOException ex) {
                LOG.error("[Trades] Не удалось удалить файл {}", prof, ex);
            }
            return true;
        }
    }

    // ---------- Для событий и меню ----------

    @Nullable
    private static List<TradeEntry> entriesFor(String prof) {
        synchronized (LOCK) {
            List<TradeEntry> l = effective(prof);
            return l == null ? null : List.copyOf(l);
        }
    }

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        ResourceLocation prof = BuiltInRegistries.VILLAGER_PROFESSION.getKey(e.getType());
        List<TradeEntry> entries = entriesFor(prof.getPath());
        if (entries == null) return;                       // файла нет, остаётся ваниль

        e.getTrades().values().forEach(List::clear);
        for (TradeEntry t : entries) {
            e.getTrades().get(t.level()).add(t.toListing());
        }
        LOG.info("[Trades] {}: подставлено торгов {}", prof, entries.size());
    }

    /** Строки меню. Пустая профессия отдаётся одной служебной строкой (level 0), чтобы осталась в списке. */
    public static List<TradeRow> rows() {
        List<TradeRow> out = new ArrayList<>();
        synchronized (LOCK) {
            Set<String> profs = new TreeSet<>(DATA.keySet());
            profs.addAll(EDITED.keySet());
            for (String prof : profs) {
                List<TradeEntry> list = effective(prof);
                if (list == null || list.isEmpty()) {
                    out.add(new TradeRow(prof, 0, -1, "minecraft:air", 1, "minecraft:air", 1, 1, false,
                            1, 0, false));
                    continue;
                }
                int[] counter = new int[6];
                for (TradeEntry t : list) {
                    int idx = counter[t.level()]++;
                    String resultId = t.result()
                            .map(r -> BuiltInRegistries.ITEM.getKey(r.item().value()).toString())
                            .orElse("minecraft:enchanted_book");
                    int resultCount = t.result().map(TradeEntry.ItemSpec::count).orElse(1);
                    String priceId = BuiltInRegistries.ITEM.getKey(t.price().item().value()).toString();
                    int cur = t.price().count();
                    out.add(new TradeRow(prof, t.level(), idx, resultId, resultCount,
                            priceId, cur, cur, t.type() != TradeEntry.Type.ITEM,
                            t.maxUses(), t.xp(), t.type() != TradeEntry.Type.ENCHANTED_BOOK));
                }
            }
        }
        return out;
    }
}