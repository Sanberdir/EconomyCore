package ru.imaginaerum.economy_core.init.structure_processor;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/** Деревни, где палатка уже поставлена, и точки спавна торговцев в таких палатках. */
public class TentVillages extends SavedData {

    private static final Factory<TentVillages> FACTORY =
            new Factory<>(TentVillages::new, TentVillages::load, null);

    private final LongOpenHashSet done = new LongOpenHashSet();
    private final LongOpenHashSet spawns = new LongOpenHashSet();

    public static TentVillages get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "economy_core_tent_villages");
    }

    public boolean isDone(long key) { return done.contains(key); }

    public void markDone(long key) {
        if (done.add(key)) setDirty();
    }

    public void addSpawn(BlockPos pos) {
        if (spawns.add(pos.asLong())) setDirty();
    }

    public List<BlockPos> getSpawns() {
        List<BlockPos> list = new ArrayList<>(spawns.size());
        for (long l : spawns) list.add(BlockPos.of(l));
        return list;
    }

    private static TentVillages load(CompoundTag tag, HolderLookup.Provider reg) {
        TentVillages d = new TentVillages();
        for (long l : tag.getLongArray("Done")) d.done.add(l);
        for (long l : tag.getLongArray("Spawns")) d.spawns.add(l);
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider reg) {
        tag.putLongArray("Done", done.toLongArray());
        tag.putLongArray("Spawns", spawns.toLongArray());
        return tag;
    }
}