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

    /** Ширина под число. Значок стоит на фиксированном месте, число не двигает его. */
    private static final int TEXT_W = 48;
    private static final int MARGIN = 6;
    private static final float MIN_SCALE = 0.4f;

    private static void drawLine(GuiGraphics g, Minecraft mc, ItemStack icon, long value, int color, int fromBottom) {
        String text = String.valueOf(value);

        int iconX = g.guiWidth() - MARGIN - TEXT_W - 18;   // не зависит от числа
        int y = g.guiHeight() - fromBottom;
        g.renderItem(icon, iconX, y);

        // чем длиннее число, тем мельче шрифт
        int w = mc.font.width(text);
        float scale = w <= TEXT_W ? 1f : Math.max(MIN_SCALE, (float) TEXT_W / w);

        var pose = g.pose();
        pose.pushPose();
        pose.translate(iconX + 18, y + (16 - 8 * scale) / 2f + 0.5f, 0);
        pose.scale(scale, scale, 1f);
        g.drawString(mc.font, text, 0, 0, color, true);
        pose.popPose();
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