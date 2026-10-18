package ru.imaginaerum.economy_core.init.tride;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TraderPriceLoader extends SimpleJsonResourceReloadListener {

    public TraderPriceLoader() {
        super(new Gson(), "trader_prices");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager rm, ProfilerFiller profiler) {
        List<JsonObject> list = new ArrayList<>();
        for (var file : new TreeMap<>(files).values()) {
            if (!file.isJsonObject()) continue;
            JsonElement entries = file.getAsJsonObject().get("entries");
            if (entries == null || !entries.isJsonArray()) continue;
            entries.getAsJsonArray().forEach(e -> { if (e.isJsonObject()) list.add(e.getAsJsonObject()); });
        }
        TraderPriceStore.setData(list);   // сам применит правила (config перекрывает датапак)
    }
}