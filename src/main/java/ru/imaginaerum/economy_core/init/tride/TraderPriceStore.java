package ru.imaginaerum.economy_core.init.tride;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Правила продажи: датапак (data/.../trader_prices) + правки config/economy_core/trader_prices.json.
 * Если файл в config есть, он полностью заменяет правила датапака.
 */
public final class TraderPriceStore {

    public static final int MAX = 9_999_999;
    private static final Logger LOG = LogUtils.getLogger();
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("economy_core").resolve("trader_prices.json");
    private static final Object LOCK = new Object();

    private static List<JsonObject> data = new ArrayList<>();
    @Nullable
    private static List<JsonObject> edited = null;

    // ---------- загрузка ----------

    /** Вызывается из TraderPriceLoader при каждом reload. */
    public static void setData(List<JsonObject> fromDatapack) {
        synchronized (LOCK) {
            data = new ArrayList<>(fromDatapack);
            edited = loadEdited();
            applyAll();
            LOG.info("[TraderPrices] Из мода: {}, из config: {}", data.size(),
                    edited == null ? "нет" : edited.size());
        }
    }

    @Nullable
    private static List<JsonObject> loadEdited() {
        if (!Files.isRegularFile(FILE)) return null;
        try (Reader r = Files.newBufferedReader(FILE)) {
            JsonElement root = JsonParser.parseReader(r);
            List<JsonObject> out = new ArrayList<>();
            if (root.isJsonObject() && root.getAsJsonObject().get("entries") instanceof JsonArray arr) {
                for (JsonElement e : arr) if (e.isJsonObject()) out.add(e.getAsJsonObject());
            }
            return out;
        } catch (Exception ex) {
            LOG.error("[TraderPrices] Ошибка чтения {}", FILE, ex);
            return null;
        }
    }

    private static void save() {
        JsonArray arr = new JsonArray();
        for (JsonObject o : edited) arr.add(o);
        JsonObject root = new JsonObject();
        root.add("entries", arr);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, PRETTY.toJson(root));
        } catch (IOException ex) {
            LOG.error("[TraderPrices] Не удалось сохранить {}", FILE, ex);
        }
    }

    private static List<JsonObject> effective() {
        return edited != null ? edited : data;
    }

    private static List<JsonObject> mutable() {
        if (edited == null) {
            edited = new ArrayList<>();
            for (JsonObject o : data) edited.add(o.deepCopy());
        }
        return edited;
    }

    private static void applyAll() {
        TraderPrices.load(effective().stream().map(JsonObject::toString).toList());
    }

    // ---------- правки (сервер) ----------



    public static boolean delete(int idx) {
        synchronized (LOCK) {
            if (idx < 0 || idx >= effective().size()) return false;
            mutable().remove(idx);
            save();
            applyAll();
            return true;
        }
    }

    /** target: "minecraft:iron_ingot", "#minecraft:swords" или "*" (любой предмет). */
    public static boolean add(String target, int count, int emeralds, String currency) {
        if (count < 1 || count > MAX || emeralds < 1 || emeralds > MAX) return false;
        String t = target.trim();
        JsonObject o = new JsonObject();
        if (t.equals("*")) {
            o.addProperty("all", true);
            o.addProperty("priority", -100);
        } else if (t.startsWith("#")) {
            if (ResourceLocation.tryParse(t.substring(1)) == null) return false;
            o.addProperty("tag", t.substring(1));
        } else {
            if (!knownItem(t)) return false;
            o.addProperty("item", t);
        }
        o.addProperty("count", count);
        o.addProperty("emeralds", emeralds);
        String cur = currency.trim();
        if (!cur.isEmpty() && !cur.equals("minecraft:emerald")) {
            if (!knownItem(cur)) return false;
            o.addProperty("currency", cur);
        }
        synchronized (LOCK) {
            mutable().add(o);
            save();
            applyAll();
            return true;
        }
    }

    /** Удаляет config-файл, возвращаются правила датапака. */
    public static boolean reset() {
        synchronized (LOCK) {
            edited = null;
            try {
                Files.deleteIfExists(FILE);
            } catch (IOException ex) {
                LOG.error("[TraderPrices] Не удалось удалить {}", FILE, ex);
            }
            applyAll();
            return true;
        }
    }

    private static boolean knownItem(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id.trim());
        return loc != null && BuiltInRegistries.ITEM.containsKey(loc)
                && BuiltInRegistries.ITEM.get(loc) != Items.AIR;
    }

    // ---------- строки для меню ----------

    public static List<SellRow> rows() {
        synchronized (LOCK) {
            List<SellRow> out = new ArrayList<>();
            List<JsonObject> l = effective();
            for (int i = 0; i < l.size(); i++) out.add(row(i, l.get(i)));
            return out;
        }
    }
    /** Правка правила из редактора: цель, партия, цена, валюта, опыт (-1 = авто). */
    public static boolean update(int idx, String target, int count, int price, String currency, int xp) {
        if (count < 1 || count > MAX || price < 1 || price > MAX || xp < -1 || xp > 9999) return false;
        String t = target.trim();
        boolean validTarget = t.equals("*")
                || (t.startsWith("#") ? ResourceLocation.tryParse(t.substring(1)) != null : knownItem(t));
        String cur = currency.trim().isEmpty() ? "minecraft:emerald" : currency.trim();
        if (!validTarget || !knownItem(cur)) return false;

        synchronized (LOCK) {
            if (idx < 0 || idx >= effective().size()) return false;
            JsonObject o = mutable().get(idx);

            boolean wasAll = o.has("all");
            o.remove("item");
            o.remove("tag");
            o.remove("all");
            if (t.equals("*")) {
                o.addProperty("all", true);
                if (!o.has("priority")) o.addProperty("priority", -100);   // запасной вариант
            } else {
                if (wasAll && num(o, "priority", 0) == -100) o.remove("priority");
                if (t.startsWith("#")) o.addProperty("tag", t.substring(1));
                else o.addProperty("item", t);
            }

            o.addProperty("count", count);
            o.addProperty("emeralds", price);
            if (cur.equals("minecraft:emerald")) o.remove("currency");
            else o.addProperty("currency", cur);
            if (xp < 0) o.remove("xp");
            else o.addProperty("xp", xp);

            save();
            applyAll();
            return true;
        }
    }
    private static SellRow row(int idx, JsonObject o) {
        int kind;
        String target;
        if (o.has("item")) { kind = 0; target = str(o, "item", "minecraft:air"); }
        else if (o.has("tag")) { kind = 1; target = str(o, "tag", "?"); }
        else { kind = 2; target = "*"; }
        boolean complex = o.has("potion") || o.has("enchants")
                || num(o, "enchant_per_level", 0) > 0;
        return new SellRow(idx, kind, target, num(o, "count", 1), num(o, "emeralds", 1),
                str(o, "currency", "minecraft:emerald"),
                o.has("deny") && o.get("deny").isJsonPrimitive() && o.get("deny").getAsBoolean(),
                complex, o.has("xp") ? num(o, "xp", -1) : -1);
    }

    private static String str(JsonObject o, String k, String def) {
        try { return o.has(k) ? o.get(k).getAsString() : def; } catch (Exception e) { return def; }
    }

    private static int num(JsonObject o, String k, int def) {
        try { return o.has(k) ? o.get(k).getAsInt() : def; } catch (Exception e) { return def; }
    }

    private TraderPriceStore() {}
}