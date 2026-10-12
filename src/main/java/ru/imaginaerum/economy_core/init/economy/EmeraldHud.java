package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.items.EcoItems;

@EventBusSubscriber(modid = EconomyCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmeraldHud {

    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);

    private static void drawLine(GuiGraphics g, Minecraft mc, ItemStack icon, int value, int color, int fromBottom) {
        String text = String.valueOf(value);
        int x = g.guiWidth() - 6 - mc.font.width(text) - 18;
        int y = g.guiHeight() - fromBottom;
        g.renderItem(icon, x, y);
        g.drawString(mc.font, text, x + 18, y + 4, color, true);
    }

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(EconomyCore.MODID, "emerald_hud"),
                (g, delta) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    // .get() вызывается уже в рантайме — предмет зарегистрирован
                    ItemStack coin = new ItemStack(EcoItems.GOLD_COIN.get());

                    drawLine(g, mc, EMERALD, EconomyBank.total(mc.player, Items.EMERALD), 0xFFFFFF, 22);
                    drawLine(g, mc, coin, EconomyBank.total(mc.player, EcoItems.GOLD_COIN.get()), 0xFFFFFF, 42);
                });
    }
}