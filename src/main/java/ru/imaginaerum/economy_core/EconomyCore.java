package ru.imaginaerum.economy_core;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import ru.imaginaerum.economy_core.init.blocks.EcoBlocks;
import ru.imaginaerum.economy_core.init.economy.EconomyAttachments;
import ru.imaginaerum.economy_core.init.entities.EconomyEntities;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;
import ru.imaginaerum.economy_core.init.entities.renderer.EconomyTraderRenderer;
import ru.imaginaerum.economy_core.init.items.EcoItems;
import ru.imaginaerum.economy_core.init.tride.EconomyMenus;
import ru.imaginaerum.economy_core.init.tride.TraderScreen;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EconomyCore.MODID)
public class EconomyCore {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "economy_core";
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public EconomyCore(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        EconomyEntities.ENTITIES.register(modEventBus);
        modEventBus.addListener(this::registerAttributes);
        EconomyMenus.MENUS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        EcoItems.ITEMS.register(modEventBus);
        EcoBlocks.BLOCKS.register(modEventBus);

        EconomyAttachments.ATTACHMENTS.register(modEventBus);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EconomyEntities.TRADER.get(), EconomyTrader.createTraderAttributes().build());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(EconomyMenus.TRADER.get(), TraderScreen::new);
        }
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EconomyEntities.TRADER.get(), EconomyTraderRenderer::new);
        }
    }
}
