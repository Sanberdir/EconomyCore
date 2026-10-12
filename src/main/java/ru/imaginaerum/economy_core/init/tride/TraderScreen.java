package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import ru.imaginaerum.economy_core.init.economy.EconomyBank;
import ru.imaginaerum.economy_core.init.items.EcoItems;

import javax.annotation.Nullable;
import java.util.List;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    private static final int ROWS = 6;
    private static final int ROW_H = 22;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 40;
    private static final int LIST_W = 252;
    private static final int SOLD_OUT_X = 84;
    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);
    private List<TraderPrices.SellLine> sellList = List.of();
    private void refreshSellList() {
        sellList = TraderPrices.forPlayer(minecraft.player);
    }
    private boolean sellTab = false;
    private int scroll = 0;
    private Button buyBtn;
    private Button sellBtn;
    @Nullable
    private ItemStack tooltip;

    public TraderScreen(TraderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 280;
        this.imageHeight = 196;
    }

    @Override
    protected void init() {
        super.init();
        buyBtn = addWidget(Button.builder(Component.translatable("gui.economy_core.trader.buy"),
                b -> setTab(false)).bounds(leftPos + 8, topPos + 16, 70, 18).build());
        sellBtn = addWidget(Button.builder(Component.translatable("gui.economy_core.trader.sell"),
                b -> setTab(true)).bounds(leftPos + 82, topPos + 16, 70, 18).build());
        updateTabs();
    }

    private void setTab(boolean sell) {
        this.sellTab = sell;
        this.scroll = 0;
        updateTabs();
    }

    private void updateTabs() {
        buyBtn.active = sellTab;   // активная вкладка «нажата»
        sellBtn.active = !sellTab;
    }

    private int total() {
        return sellTab ? sellList.size() : menu.getOffers().size();
    }
    // ---------- Отрисовка ----------

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        tooltip = null;
        refreshSellList();
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1E1E1E);
        g.renderOutline(x, y, imageWidth, imageHeight, 0xFF8B8B8B);
        g.drawString(font, title, x + 8, y + 5, 0xFFFFFF);

// Изумруды игрока
        g.renderItem(EMERALD, x + imageWidth - 52, y + 16);
        g.drawString(font, String.valueOf(countEmeralds()), x + imageWidth - 34, y + 20, 0x55FF55);
        g.renderItem(new ItemStack(EcoItems.GOLD_COIN.get()), x + imageWidth - 52, y + 2);
        g.drawString(font, String.valueOf(EconomyBank.total(minecraft.player, EcoItems.GOLD_COIN.get())),
                x + imageWidth - 34, y + 6, 0xFFD700);
        buyBtn.render(g, mx, my, pt);
        sellBtn.render(g, mx, my, pt);

        int total = total();
        scroll = Mth.clamp(scroll, 0, Math.max(0, total - ROWS));

        if (total == 0) {
            g.drawString(font, Component.translatable("gui.economy_core.trader.empty_buy"),
                    x + LIST_X + 4, y + LIST_Y + 6, 0xAAAAAA);
        }

        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= total) break;
            renderRow(g, idx, x + LIST_X, y + LIST_Y + i * ROW_H, mx, my);
        }

        // Полоса прокрутки
        if (total > ROWS) {
            int barH = ROWS * ROW_H - 2;
            int thumbH = Math.max(10, barH * ROWS / total);
            int thumbY = (barH - thumbH) * scroll / (total - ROWS);
            int bx = x + LIST_X + LIST_W + 3;
            g.fill(bx, y + LIST_Y, bx + 4, y + LIST_Y + barH, 0xFF333333);
            g.fill(bx, y + LIST_Y + thumbY, bx + 4, y + LIST_Y + thumbY + thumbH, 0xFFAAAAAA);
        }

        g.drawString(font, Component.translatable(sellTab
                        ? "gui.economy_core.trader.sell_hint"
                        : "gui.economy_core.trader.buy_hint"),
                x + 8, y + imageHeight - 14, 0x888888);

        if (tooltip != null) {
            g.renderTooltip(font, tooltip, mx, my);
        }
    }

    private void renderRow(GuiGraphics g, int idx, int rx, int ry, int mx, int my) {
        boolean hovered = mx >= rx && mx < rx + LIST_W && my >= ry && my < ry + ROW_H - 2;
        g.fill(rx, ry, rx + LIST_W, ry + ROW_H - 2, hovered ? 0xFF4A4A4A : 0xFF333333);
        int iy = ry + 2;

        // правая колонка шириной 70 px под статус («Распродано», «У вас: N»)
        int statusW = 70;
        int statusX = rx + LIST_W - statusW;

        if (sellTab) {
            TraderPrices.SellLine e = sellList.get(idx);
            ItemStack in = e.stack();                                   // уже с размером партии
            ItemStack out = new ItemStack(e.quote().currency(), e.quote().amount());
            int have = e.have();
            boolean ok = have >= e.quote().count();

            item(g, in, rx + 4, iy, mx, my);
            g.drawString(font, "→", rx + 26, iy + 4, 0xAAAAAA);
            item(g, out, rx + 38, iy, mx, my);
            g.drawString(font, font.plainSubstrByWidth(in.getHoverName().getString(), statusX - (rx + 62) - 4),
                    rx + 62, iy + 4, ok ? 0xFFFFFF : 0x777777);

            Component haveText = Component.translatable("gui.economy_core.trader.have", have);
            g.drawString(font, haveText, rx + LIST_W - 4 - font.width(haveText), iy + 4,
                    ok ? 0x55FF55 : 0x777777);
        } else {
            MerchantOffer o = menu.getOffers().get(idx);
            ItemStack a = o.getCostA();
            ItemStack b = o.getCostB();
            ItemStack r = o.getResult();
            boolean soldOut = menu.getUses(idx) >= o.getMaxUses();
            boolean afford = has(a) && (b.isEmpty() || has(b));

            item(g, a, rx + 4, iy, mx, my);
            item(g, b, rx + 26, iy, mx, my);
            g.drawString(font, "→", rx + 48, iy + 4, 0xAAAAAA);

            item(g, r, rx + 62, iy, mx, my);

            if (soldOut) {
                g.drawString(font, Component.translatable("gui.economy_core.trader.sold_out"),
                        rx + SOLD_OUT_X, iy + 4, 0xFF5555);
            } else {
                int nameW = LIST_W - 84 - 4;
                g.drawString(font, font.plainSubstrByWidth(r.getHoverName().getString(), nameW),
                        rx + 84, iy + 4, afford ? 0xFFFFFF : 0x777777);
            }
        }
    }

    private void item(GuiGraphics g, ItemStack s, int x, int y, int mx, int my) {
        if (s.isEmpty()) return;
        g.renderItem(s, x, y);
        g.renderItemDecorations(font, s, x, y);
        if (mx >= x && mx < x + 16 && my >= y && my < y + 16) {
            tooltip = s;
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        // всё рисуется в render()
    }

    // ---------- Ввод ----------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int idx = rowAt(mx, my);
            if (idx >= 0) {
                int id;
                if (sellTab) {
                    id = (hasShiftDown() ? TraderMenu.SELL_ALL : TraderMenu.SELL_ONE) + sellList.get(idx).slot();
                } else {
                    id = (hasShiftDown() ? TraderMenu.BUY_ALL : 0) + idx;
                }
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, Math.max(0, total() - ROWS));
        return true;
    }

    private int rowAt(double mx, double my) {
        double lx = mx - leftPos - LIST_X;
        double ly = my - topPos - LIST_Y;
        if (lx < 0 || lx >= LIST_W || ly < 0 || ly >= ROWS * ROW_H) return -1;
        int idx = scroll + (int) (ly / ROW_H);
        return idx < total() ? idx : -1;
    }

    private int countEmeralds() {
        return EconomyBank.total(minecraft.player, Items.EMERALD);
    }

    private boolean has(ItemStack need) {
        if (EconomyBank.isCurrency(need)) {
            return EconomyBank.total(minecraft.player, need.getItem()) >= need.getCount();
        }
        int n = 0;
        for (ItemStack s : minecraft.player.getInventory().items) {
            if (ItemStack.isSameItem(s, need)) n += s.getCount();
        }
        return n >= need.getCount();
    }



    private int countItem(net.minecraft.world.item.Item item) {
        int n = 0;
        for (ItemStack s : minecraft.player.getInventory().items) {
            if (s.is(item)) n += s.getCount();
        }
        return n;
    }
}