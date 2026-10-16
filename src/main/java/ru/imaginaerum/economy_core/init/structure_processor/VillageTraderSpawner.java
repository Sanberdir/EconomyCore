package ru.imaginaerum.economy_core.init.structure_processor;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.entities.EconomyEntities;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class VillageTraderSpawner {

    private static final int CHECK_PERIOD = 20;
    private static final int RECHECK = 1200;
    private static final double TENT_RADIUS = 4;
    private static final BlockPos LOCAL_SPAWN = new BlockPos(2, 1, 3);
    private static final ResourceLocation TENT_ID =
            ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "merchant_tent");

    private static final Map<UUID, Long> NEXT = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (p.tickCount % CHECK_PERIOD != 0) return;

        ServerLevel level = p.serverLevel();
        var start = level.structureManager()
                .getStructureWithPieceAt(p.blockPosition(), StructureTags.VILLAGE);

        if (!start.isValid()) {
            NEXT.remove(p.getUUID());
            return;
        }

        long now = level.getGameTime();
        Long next = NEXT.get(p.getUUID());
        if (next != null && now < next) return;

        NEXT.put(p.getUUID(), now + RECHECK);

        LogUtils.getLogger().info("[Trader] === Тик проверки для {} в деревне {}..{} ===",
                p.getName().getString(),
                start.getBoundingBox().minX(), start.getBoundingBox().maxX());

        trySpawn(level, p, start);
    }

    private static void trySpawn(ServerLevel level, ServerPlayer p, StructureStart start) {
        BoundingBox village = start.getBoundingBox();
        TentVillages data = TentVillages.get(level);
        List<BlockPos> spots = new ArrayList<>();

        // 1. Палатки, которые поставил jigsaw (houses.json)
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof PoolElementStructurePiece pp
                    && pp.getElement().toString().contains("merchant_tent")) {
                StructurePlaceSettings s = new StructurePlaceSettings().setRotation(pp.getRotation());
                spots.add(toWorld(s, pp.getPosition()));
            }
        }

        // 2. Палатки, которые поставил ensureTent
        for (BlockPos saved : data.getSpawns()) {
            if (village.isInside(saved)) spots.add(saved);
        }

        if (spots.isEmpty()) {
            LogUtils.getLogger().info("[Trader] Палатки нет → вызываю ensureTent");
            ensureTent(level, start);
            return;
        }

        for (BlockPos spot : spots) spawnIfAbsent(level, spot);
    }

    /** Локальная точка (2,1,3) шаблона → мировая позиция с учётом поворота и origin. */
    private static BlockPos toWorld(StructurePlaceSettings settings, BlockPos origin) {
        return StructureTemplate.calculateRelativePosition(settings, LOCAL_SPAWN).offset(origin);
    }

    private static void spawnIfAbsent(ServerLevel level, BlockPos spot) {
        if (!level.hasChunkAt(spot)) return;
        if (!level.getEntitiesOfClass(EconomyTrader.class, new AABB(spot).inflate(TENT_RADIUS)).isEmpty()) return;

        EconomyTrader trader = EconomyEntities.TRADER.get().spawn(level, spot, MobSpawnType.EVENT);
        if (trader != null) {
            trader.setPersistenceRequired();
            trader.setTentPos(spot.below());
            LogUtils.getLogger().info("[Trader] Спавн торговца на {}", spot);
        }
    }
    /** Сдвиг Y палатки относительно верхнего блока земли (0 = слой 0 палатки на месте верхнего блока земли). */
    private static final int TENT_Y_OFFSET = 0;
    private static final int SEARCH_ATTEMPTS = 60;
    private static final int SEARCH_MARGIN = 8;   // насколько искать за пределами bounding box деревни
    private static final int HOUSE_GAP = 2;       // минимальный зазор до других кусков
    private static final int MAX_SLOPE = 2;
    private static void ensureTent(ServerLevel level, StructureStart start) {
        LogUtils.getLogger().info("[Trader] ensureTent: старт");

        TentVillages data = TentVillages.get(level);
        long key = start.getChunkPos().toLong();
        if (data.isDone(key)) return;

        // --- Шаблон ---
        var tplOpt = level.getStructureManager().get(TENT_ID);
        if (tplOpt.isEmpty()) {
            LogUtils.getLogger().error("[Trader] ensureTent: НЕТ ШАБЛОНА {}", TENT_ID);
            data.markDone(key);
            return;
        }
        StructureTemplate tpl = tplOpt.get();

        BoundingBox vb = start.getBoundingBox();
        List<BoundingBox> occupied = new ArrayList<>();
        for (StructurePiece piece : start.getPieces()) occupied.add(piece.getBoundingBox());

        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            Rotation rot = Rotation.getRandom(level.random);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rot)
                    .setIgnoreEntities(true);

            BoundingBox local = tpl.getBoundingBox(settings, BlockPos.ZERO);
            int spanX = local.getXSpan(), spanZ = local.getZSpan();

            int cx = vb.minX() - SEARCH_MARGIN
                    + level.random.nextInt(vb.getXSpan() - spanX + 2 * SEARCH_MARGIN + 1);
            int cz = vb.minZ() - SEARCH_MARGIN
                    + level.random.nextInt(vb.getZSpan() - spanZ + 2 * SEARCH_MARGIN + 1);
            int endX = cx + spanX - 1, endZ = cz + spanZ - 1;

            // 1. Чанки площадки загружены
            if (!level.hasChunksAt(new BlockPos(cx, level.getMinBuildHeight(), cz),
                    new BlockPos(endX, level.getMinBuildHeight(), endZ))) continue;

            // 2. Не пересекается с другими кусками деревни (по XZ, с зазором)
            if (overlapsAny(occupied, cx, cz, endX, endZ)) continue;

            // 3. Рельеф: суша, без деревьев, небольшой перепад
            int minH = Integer.MAX_VALUE, maxH = Integer.MIN_VALUE;
            boolean ok = true;
            int[][] heights = new int[spanX][spanZ];
            for (int x = cx; x <= endX && ok; x++) {
                for (int z = cz; z <= endZ; z++) {
                    int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockState ground = level.getBlockState(new BlockPos(x, h - 1, z));
                    if (!ground.getFluidState().isEmpty()
                            || ground.is(BlockTags.LEAVES) || ground.is(BlockTags.LOGS)
                            || ground.isAir()) { ok = false; break; }
                    heights[x - cx][z - cz] = h;
                    minH = Math.min(minH, h);
                    maxH = Math.max(maxH, h);
                }
            }
            if (!ok || maxH - minH > MAX_SLOPE) continue;

            // --- Подходящее место найдено ---
            int originY = maxH - 1 + TENT_Y_OFFSET;
            BlockPos origin = new BlockPos(cx - local.minX(), originY - local.minY(), cz - local.minZ());
            BoundingBox tb = tpl.getBoundingBox(settings, origin);

            // Подсыпаем землю под низкие колонки
            for (int x = cx; x <= endX; x++) {
                for (int z = cz; z <= endZ; z++) {
                    for (int y = heights[x - cx][z - cz]; y <= tb.minY(); y++) {
                        level.setBlock(new BlockPos(x, y, z), Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
            }
            // Расчищаем объём палатки (деревья, трава, цветы)
            for (BlockPos pos : BlockPos.betweenClosed(tb.minX(), tb.minY(), tb.minZ(),
                    tb.maxX(), tb.maxY(), tb.maxZ())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }

            boolean placed = tpl.placeInWorld(level, origin, origin, settings, level.random, 3);
            LogUtils.getLogger().info("[Trader] ensureTent: placeInWorld={} origin={} rot={}", placed, origin, rot);

            replaceJigsawBlocks(level, tb);

            if (placed) {
                BlockPos spot = toWorld(settings, origin);
                data.addSpawn(spot);
                spawnIfAbsent(level, spot);
            }
            data.markDone(key);
            LogUtils.getLogger().info("[Trader] ensureTent: деревня {} помечена done", key);
            return;
        }

        LogUtils.getLogger().info("[Trader] ensureTent: свободного места не нашлось, повторю позже");
    }

    private static boolean overlapsAny(List<BoundingBox> boxes, int minX, int minZ, int maxX, int maxZ) {
        for (BoundingBox b : boxes) {
            if (minX <= b.maxX() + HOUSE_GAP && maxX >= b.minX() - HOUSE_GAP
                    && minZ <= b.maxZ() + HOUSE_GAP && maxZ >= b.minZ() - HOUSE_GAP) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проходит по bounding box палатки и заменяет все jigsaw / structure_block
     * на воздух. Также убирает structure_void, если он остался.
     */
    private static void replaceJigsawBlocks(ServerLevel level, BoundingBox tb) {
        int replaced = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                new BlockPos(tb.minX(), tb.minY(), tb.minZ()),
                new BlockPos(tb.maxX(), tb.maxY(), tb.maxZ()))) {
            var state = level.getBlockState(pos);
            if (state.is(Blocks.JIGSAW)
                    || state.is(Blocks.STRUCTURE_BLOCK)
                    || state.is(Blocks.STRUCTURE_VOID)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                replaced++;
            }
        }
        LogUtils.getLogger().info("[Trader] replaceJigsawBlocks: заменено {} блоков", replaced);
    }

    @SubscribeEvent
    public static void onLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e) {
        NEXT.remove(e.getEntity().getUUID());
    }
}