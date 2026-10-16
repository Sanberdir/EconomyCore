package ru.imaginaerum.economy_core.init.structure_processor;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.entities.EconomyEntities;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;

@EventBusSubscriber(modid = EconomyCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class TraderSpawnRules {

    private static final int MIN_DISTANCE_BETWEEN = 128;

    @SubscribeEvent
    public static void register(RegisterSpawnPlacementsEvent e) {
        e.register(EconomyEntities.TRADER.get(),                       // <- твой регистр EntityType
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> canSpawn(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean canSpawn(ServerLevelAccessor level, BlockPos pos) {
        // 1. Только на полированном граните
        if (!level.getBlockState(pos.below()).is(Blocks.POLISHED_GRANITE)) return false;

        // 2. Только внутри деревни
        if (!isInVillage(level, pos)) return false;

        // 3. Не больше одного торговца на деревню
        var box = new net.minecraft.world.phys.AABB(pos).inflate(MIN_DISTANCE_BETWEEN);
        return level.getEntitiesOfClass(EconomyTrader.class, box).isEmpty();
    }

    private static boolean isInVillage(LevelAccessor level, BlockPos pos) {
        return level instanceof net.minecraft.server.level.ServerLevel sl
                ? sl.structureManager().getStructureWithPieceAt(pos, StructureTags.VILLAGE).isValid()
                : level instanceof net.minecraft.server.level.WorldGenRegion wg
                && wg.getLevel().structureManager()
                .getStructureWithPieceAt(pos, StructureTags.VILLAGE).isValid();
    }

    private TraderSpawnRules() {}
}