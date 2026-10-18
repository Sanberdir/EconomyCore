package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import ru.imaginaerum.economy_core.init.economy.replace_trades.ItemPickerScreen;

public class AddSellScreen extends Screen {

    private static final int LABEL_W = 96;

    private final Screen parent;
    private EditBox target, count, price, currency;
    private String vTarget = "", vCount = "1", vPrice = "1", vCurrency = "minecraft:emerald";

    public AddSellScreen(Screen parent) {
        super(Component.translatable("gui.economy_core.buy.add.title"));
        this.parent = parent;
    }

    private void save() {
        if (target == null) return;
        vTarget = target.getValue();
        vCount = count.getValue();
        vPrice = price.getValue();
        vCurrency = currency.getValue();
    }

    private EditBox box(int x, int y, int w, String value, boolean numeric) {
        EditBox b = new EditBox(font, x, y, w, 18, Component.empty());
        b.setMaxLength(numeric ? 7 : 100);
        if (numeric) b.setFilter(s -> s.matches("\\d*"));
        b.setValue(value);
        addRenderableWidget(b);
        return b;
    }

    private EditBox itemBox(int x, int y, String value, boolean forTarget) {
        EditBox b = box(x, y, 112, value, false);
        addRenderableWidget(Button.builder(Component.literal("…"),
                        btn -> minecraft.setScreen(new ItemPickerScreen(this, forTarget,
                                id -> (forTarget ? target : currency).setValue(id))))
                .bounds(x + 116, y - 1, 20, 20).build());
        if (forTarget) {
            addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add.hand"),
                    btn -> fromHand()).bounds(x + 138, y - 1, 36, 20).build());
        }
        return b;
    }

    @Override
    protected void init() {
        save();
        int x = width / 2 - 140 + LABEL_W;
        int y = height / 2 - 60;

        target = itemBox(x, y, vTarget, true);
        count = box(x, y + 26, 60, vCount, true);
        price = box(x, y + 52, 60, vPrice, true);
        currency = itemBox(x, y + 78, vCurrency, false);

        addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add.confirm"),
                b -> confirm()).bounds(width / 2 - 84, y + 112, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add.cancel"),
                b -> onClose()).bounds(width / 2 + 4, y + 112, 80, 20).build());
        setInitialFocus(target);
    }

    private void fromHand() {
        ItemStack s = minecraft.player.getMainHandItem();
        if (!s.isEmpty()) target.setValue(BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
    }

    private static int num(EditBox b) {
        try {
            return Integer.parseInt(b.getValue().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean known(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id.trim());
        return loc != null && BuiltInRegistries.ITEM.containsKey(loc);
    }

    /** Иконка только для обычных предметов; для «*» сундук, для тегов пусто. */
    private static ItemStack preview(String id) {
        String t = id.trim();
        if (t.isEmpty() || t.startsWith("#")) return ItemStack.EMPTY;
        if (t.equals("*")) return new ItemStack(Items.CHEST);
        ResourceLocation loc = ResourceLocation.tryParse(t);
        if (loc == null) return new ItemStack(Items.BARRIER);
        return BuiltInRegistries.ITEM.getOptional(loc)
                .map(i -> new ItemStack(i))
                .orElseGet(() -> new ItemStack(Items.BARRIER));
    }

    private void confirm() {
        String t = target.getValue().trim();
        // "#тег" и "*" проверяет сервер, предметы проверяем здесь
        boolean okT = t.equals("*") || (t.startsWith("#") ? t.length() > 1 : known(t));
        boolean okC = known(currency.getValue());
        target.setTextColor(okT ? 0xE0E0E0 : 0xFF5555);
        currency.setTextColor(okC ? 0xE0E0E0 : 0xFF5555);

        int c = num(count), p = num(price);
        if (!okT || !okC || c < 1 || p < 1) return;

        minecraft.setScreen(parent);
        PacketDistributor.sendToServer(SellEditPayload.add(t, c, p, currency.getValue().trim()));
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        int x0 = width / 2 - 140;
        int y = height / 2 - 60;
        g.drawCenteredString(font, title, width / 2, y - 22, 0xFFFFFF);

        String[] labels = {"target", "count", "price", "currency"};
        int[] ys = {0, 26, 52, 78};
        for (int i = 0; i < labels.length; i++) {
            g.drawString(font, Component.translatable("gui.economy_core.buy.add." + labels[i]),
                    x0, y + ys[i] + 5, 0xAAAAAA);
        }

        int px = x0 + LABEL_W + 178;
        g.renderItem(preview(target.getValue()), px, y + 1);
        g.renderItem(preview(currency.getValue()), px, y + 79);
    }
}