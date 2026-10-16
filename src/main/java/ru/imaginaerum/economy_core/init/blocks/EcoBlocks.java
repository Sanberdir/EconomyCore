package ru.imaginaerum.economy_core.init.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.items.EcoItems;

import java.util.function.Supplier;

public class EcoBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(EconomyCore.MODID);

    public static final DeferredBlock<Block> PILE_OF_GOLD = registerBlock("pile_of_gold",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.4F)
                    .sound(SoundType.STONE)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        EcoItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
