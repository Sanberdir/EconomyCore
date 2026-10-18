package ru.imaginaerum.economy_core.init.economy.replace_trades;

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

public class AddTradeScreen extends Screen {

    private static final int LABEL_W = 96;

    private final Screen parent;
    private final String profession;
    private final int level;
    private EditBox result, resultCount, price, priceCount, maxUses, xp;

    // значения переживают пересборку виджетов (возврат из выбора предмета, смена размера окна)
    private String vResult = "", vResultCount = "1", vPrice = "economy_core:gold_coin",
            vPriceCount = "100", vMaxUses = "12", vXp = "1";

    public AddTradeScreen(Screen parent, String profession, int level) {
        super(Component.translatable("gui.economy_core.sell.add.title"));
        this.parent = parent;
        this.profession = profession;
        this.level = level;
    }

    private void save() {
        if (result == null) return;
        vResult = result.getValue();
        vResultCount = resultCount.getValue();
        vPrice = price.getValue();
        vPriceCount = priceCount.getValue();
        vMaxUses = maxUses.getValue();
        vXp = xp.getValue();
    }

    private EditBox box(int x, int y, int w, String value, boolean numeric) {
        EditBox b = new EditBox(font, x, y, w, 18, Component.empty());
        b.setMaxLength(numeric ? 7 : 100);
        if (numeric) b.setFilter(s -> s.matches("\\d*"));
        b.setValue(value);
        addRenderableWidget(b);
        return b;
    }

    /** Поле id + кнопка выбора из сетки + кнопка «из руки». */
    private EditBox itemBox(int x, int y, String value, boolean forResult) {
        EditBox b = box(x, y, 112, value, false);
        addRenderableWidget(Button.builder(Component.literal("…"),
                        btn -> minecraft.setScreen(new ItemPickerScreen(this, false,
                                id -> (forResult ? result : price).setValue(id))))
                .bounds(x + 116, y - 1, 20, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add.hand"),
                btn -> fromHand(forResult)).bounds(x + 138, y - 1, 36, 20).build());
        return b;
    }

    @Override
    protected void init() {
        save();
        int x = width / 2 - 140 + LABEL_W;
        int y = height / 2 - 86;

        result = itemBox(x, y, vResult, true);
        resultCount = box(x, y + 26, 60, vResultCount, true);
        price = itemBox(x, y + 52, vPrice, false);
        priceCount = box(x, y + 78, 60, vPriceCount, true);
        maxUses = box(x, y + 104, 60, vMaxUses, true);
        xp = box(x, y + 130, 60, vXp, true);

        addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add.confirm"),
                b -> confirm()).bounds(width / 2 - 84, y + 160, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add.cancel"),
                b -> onClose()).bounds(width / 2 + 4, y + 160, 80, 20).build());
        setInitialFocus(result);
    }

    private void fromHand(boolean forResult) {
        ItemStack s = minecraft.player.getMainHandItem();
        if (s.isEmpty()) return;
        (forResult ? result : price).setValue(BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
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

    private static ItemStack preview(String id) {
        if (id.isBlank()) return ItemStack.EMPTY;
        ResourceLocation loc = ResourceLocation.tryParse(id.trim());
        if (loc == null) return new ItemStack(Items.BARRIER);
        return BuiltInRegistries.ITEM.getOptional(loc)
                .map(i -> new ItemStack(i))
                .orElseGet(() -> new ItemStack(Items.BARRIER));
    }

    private void confirm() {
        boolean okR = known(result.getValue());
        boolean okP = known(price.getValue());
        result.setTextColor(okR ? 0xE0E0E0 : 0xFF5555);
        price.setTextColor(okP ? 0xE0E0E0 : 0xFF5555);

        int rc = num(resultCount), pc = num(priceCount), mu = num(maxUses), xpv = num(xp);
        if (!okR || !okP || rc < 1 || pc < 1 || mu < 1 || xpv < 0) return;

        String rid = result.getValue().trim(), pid = price.getValue().trim();
        minecraft.setScreen(parent);          // сначала возвращаемся, чтобы ответ сервера обновил редактор
        PacketDistributor.sendToServer(
                TradeEditPayload.add(profession, level, rid, rc, pid, pc, mu, xpv));
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
        int y = height / 2 - 86;
        g.drawCenteredString(font, title, width / 2, y - 22, 0xFFFFFF);

        String[] labels = {"result", "result_count", "price", "price_count", "max_uses", "xp"};
        int[] ys = {0, 26, 52, 78, 104, 130};
        for (int i = 0; i < labels.length; i++) {
            g.drawString(font, Component.translatable("gui.economy_core.sell.add." + labels[i]),
                    x0, y + ys[i] + 5, 0xAAAAAA);
        }

        int px = x0 + LABEL_W + 178;
        g.renderItem(preview(result.getValue()), px, y + 1);
        g.renderItem(preview(price.getValue()), px, y + 53);
    }
}