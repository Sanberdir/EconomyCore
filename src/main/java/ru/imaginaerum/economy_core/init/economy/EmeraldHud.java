package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmeraldHud {

    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "emerald_hud"),
                (g, delta) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    String text = String.valueOf(EconomyBank.total(mc.player));
                    int w = mc.font.width(text);
                    int x = g.guiWidth() - 6 - w - 18;
                    int y = g.guiHeight() - 22;

                    g.renderItem(EMERALD, x, y);
                    g.drawString(mc.font, text, x + 18, y + 4, 0x55FF55, true);
                });
    }
}