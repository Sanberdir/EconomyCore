package ru.imaginaerum.economy_core.init.tride;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import ru.imaginaerum.economy_core.init.economy.EconomyBank;
import ru.imaginaerum.economy_core.init.items.EcoItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    // ---- сетка лотов ----
    private static final int COLS = 7;
    private static final int ROWS = 3;
    private static final int CELL_W = 36;
    private static final int CELL_H = 36;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 22;
    private static final int GRID_W = COLS * CELL_W;
    private static final int GRID_H = ROWS * CELL_H;

    // ---- инвентарь ----
    private static final int SLOT = 18;
    private static final int INV_X = (280 - 9 * SLOT) / 2;
    private static final int INV_LABEL_Y = LIST_Y + GRID_H + 6;
    private static final int INV_Y = INV_LABEL_Y + 12;
    private static final int HOTBAR_GAP = 0;
    private int invScroll = 0;

    private int sellRows() { return (sellList.size() + COLS - 1) / COLS; }
    private int sellMaxScroll() { return Math.max(0, sellRows() - EQ_ROWS); }
    // ---- вкладки: инвентарь / снаряжение ----
    private static final int TAB_H = 12, TAB_INV_W = 80, TAB_EQ_W = 90;
    private static final int EQ_ROWS = 2;   // 2 ряда по 36 px = 72 px, как инвентарь
    private boolean equipTab = false;
    private List<EquipLine> equipLines = List.of();
    private int equipScroll = 0;
    private int eqTotalRows() { return (equipLines.size() + COLS - 1) / COLS; }
    private int eqMaxScroll() { return Math.max(0, eqTotalRows() - EQ_ROWS); }
    // ---- диалог ----
    private static final int DLG_W = 176;
    private static final int DLG_H = 110;

    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);

    private record Lot(ItemStack item, ItemStack priceA, ItemStack priceB,
                       int stock, int maxQty, boolean sell, boolean soldOut, int have) {
        boolean affordable() { return !soldOut && maxQty > 0; }
    }

    private List<TraderPrices.SellLine> sellList = List.of();
    private int scroll = 0;

    /** -1 — диалог закрыт. Покупка: индекс торга. Продажа: слот инвентаря. */
    private int dialogKey = -1;
    private boolean dialogSell = false;
    private int qty = 1;
    private boolean qtyFieldFocused = false;
    private String qtyFieldText = "";
    private static final int QTY_FIELD_MAX_LEN = 4;

    public TraderScreen(TraderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 280;
        this.imageHeight = INV_Y + 4 * SLOT + 22;
    }

    private void refreshSellList() {
        sellList = TraderPrices.forPlayer(minecraft.player);
    }

    private int total() {
        return menu.getOffers().size();
    }

    private int totalRows() {
        return (total() + COLS - 1) / COLS;
    }

    private int maxScroll() {
        return Math.max(0, totalRows() - ROWS);
    }

    // =====================================================================
    //  Лоты
    // =====================================================================

    @Nullable
    private Lot buyLot(int idx) {
        if (idx < 0 || idx >= menu.getOffers().size()) return null;

        MerchantOffer o = menu.getOffers().get(idx);
        int stock = o.getMaxUses() - menu.getUses(idx);
        ItemStack a = TradePricing.costA(o);
        ItemStack b = TradePricing.costB(o);
        int m = Math.max(0, stock);
        if (!a.isEmpty()) m = Math.min(m, owned(a) / Math.max(1, a.getCount()));
        if (!b.isEmpty()) m = Math.min(m, owned(b) / Math.max(1, b.getCount()));

        return new Lot(o.getResult(), a, b, stock, Math.min(m, 999), false, stock <= 0, -1);
    }

    private Lot sellLot(TraderPrices.SellLine e) {
        var q = e.quote();
        int batches = Math.min(999, e.have() / Math.max(1, q.count()));
        return new Lot(e.stack(), new ItemStack(q.currency(), q.amount()), ItemStack.EMPTY,
                -1, batches, true, false, e.have());
    }

    /** Строка продажи для предмета из инвентаря (null — торговец это не покупает). */
    @Nullable
    private TraderPrices.SellLine sellLineFor(ItemStack s) {
        if (s.isEmpty()) return null;
        for (TraderPrices.SellLine e : sellList) {
            if (ItemStack.isSameItemSameComponents(e.stack(), s)) return e;
        }
        return null;
    }

    @Nullable
    private Lot dialogLot() {
        if (dialogKey < 0) return null;
        if (dialogSell) {
            for (TraderPrices.SellLine e : sellList) {
                if (e.slot() == dialogKey) return sellLot(e);
            }
            return null;
        }
        return buyLot(dialogKey);
    }

    private int owned(ItemStack need) {
        if (EconomyBank.isCurrency(need)) {
            return EconomyBank.total(minecraft.player, need.getItem());
        }
        int n = 0;
        for (ItemStack s : minecraft.player.getInventory().items) {
            if (ItemStack.isSameItem(s, need)) n += s.getCount();
        }
        return n;
    }

    // =====================================================================
    //  Раскладка инвентаря
    // =====================================================================

    private int invSlotX(int i) {
        return leftPos + INV_X + (i < 9 ? i : (i - 9) % 9) * SLOT;
    }

    private int invSlotY(int i) {
        int base = topPos + INV_Y;
        if (i < 9) return base + 3 * SLOT + HOTBAR_GAP;
        return base + ((i - 9) / 9) * SLOT;
    }

    // =====================================================================
    //  Вкладки
    // =====================================================================

    private int tabY() { return topPos + INV_LABEL_Y - 2; }
    private int tabInvX() { return leftPos + LIST_X; }
    private int tabEqX() { return leftPos + LIST_X + TAB_INV_W + 4; }
    private int areaX() { return leftPos + LIST_X; }
    private int areaY() { return topPos + INV_Y; }
    private int areaW() { return imageWidth - 16; }

    public void setEquipLines(List<EquipLine> l) {
        equipLines = l;
        equipScroll = Mth.clamp(equipScroll, 0, eqMaxScroll());
    }

    private static ItemStack itemOf(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        Item item = loc == null ? Items.BARRIER : BuiltInRegistries.ITEM.getOptional(loc).orElse(Items.BARRIER);
        return new ItemStack(item);
    }

    private void selectTab(boolean equip) {
        if (equipTab == equip) return;
        System.out.println("[EQ] selectTab equip=" + equip + " wasEquip=" + equipTab);
        if (equipTab) {
            PacketDistributor.sendToServer(EquipActionPayload.commit());   // ДО смены флага
        }
        equipTab = equip;
        equipScroll = 0;
        invScroll = 0;
        equipLines = List.of();
        if (equip) PacketDistributor.sendToServer(EquipActionPayload.list());
        click();
    }

    private void renderTabs(GuiGraphics g, int mx, int my) {
        int y = tabY();
        drawTab(g, tabInvX(), y, TAB_INV_W, "gui.economy_core.trader.tab.inventory", !equipTab, mx, my);
        drawTab(g, tabEqX(), y, TAB_EQ_W, "gui.economy_core.trader.tab.equipment", equipTab, mx, my);
    }

    private void drawTab(GuiGraphics g, int x, int y, int w, String key, boolean active, int mx, int my) {
        boolean hov = hit(mx, my, x, y, w, TAB_H);
        g.fill(x, y, x + w, y + TAB_H, active ? 0xFF55555A : hov ? 0xFF444448 : 0xFF333333);
        g.renderOutline(x, y, w, TAB_H, active ? 0xFFFFFFFF : 0xFF777777);
        g.drawCenteredString(font, font.plainSubstrByWidth(Component.translatable(key).getString(), w - 4),
                x + w / 2, y + 2, active ? 0xFFFFFF : 0xAAAAAA);
    }

    /** Рисует список надетого и проданного. Возвращает предмет под курсором для подсказки. */
    private ItemStack renderEquipList(GuiGraphics g, int mx, int my) {
        int ax = areaX(), ay = areaY(), aw = areaW();
        equipScroll = Mth.clamp(equipScroll, 0, eqMaxScroll());

        if (equipLines.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("gui.economy_core.trader.equip.none"),
                    ax + aw / 2, ay + 28, 0xAAAAAA);
            return ItemStack.EMPTY;
        }

        ItemStack tip = ItemStack.EMPTY;
        for (int r = 0; r < EQ_ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int n = (equipScroll + r) * COLS + c;
                if (n >= equipLines.size()) continue;
                EquipLine l = equipLines.get(n);
                int cx = ax + c * CELL_W, cy = ay + r * CELL_H;
                boolean can = l.sold() || l.amount() > 0;
                boolean hov = hit(mx, my, cx, cy, CELL_W, CELL_H);

                int bg = l.sold() ? (hov ? 0xFF3F5A3F : 0xFF2F442F)
                        : (hov && can ? 0xFF55555A : 0xFF333333);
                g.fill(cx, cy, cx + CELL_W - 1, cy + CELL_H - 1, bg);
                if (hov && can) g.renderOutline(cx, cy, CELL_W - 1, CELL_H - 1, 0xFFFFFFFF);

                int ix = cx + (CELL_W - 1 - 16) / 2, iy = cy + 3;
                g.renderItem(l.stack(), ix, iy);
                g.renderItemDecorations(font, l.stack(), ix, iy);

                int py = cy + 24;
                if (can) {
                    String t = String.valueOf(l.amount());
                    float iconScale = 0.5f;
                    int maxTextW = CELL_W - 1 - 2 - Math.round(16 * iconScale) - 1;
                    float ts = fitScale(t, maxTextW);
                    int w = costWidth(t, iconScale, ts);
                    drawCost(g, itemOf(l.currency()), t, l.sold() ? 0xFFAA55 : 0x55FF55,
                            cx + (CELL_W - 1 - w) / 2, py, iconScale, ts);
                    if (l.sold()) g.drawString(font, "↩", cx + 2, cy + 2, 0xFFAA55);
                } else {
                    g.drawCenteredString(font, "✘", cx + (CELL_W - 1) / 2, py, 0x888888);
                }
                if (hov) tip = l.stack();
            }
        }

        int max = eqMaxScroll();
        if (max > 0) {
            int barH = EQ_ROWS * CELL_H - 2;
            int thumb = Math.max(10, barH * EQ_ROWS / eqTotalRows());
            int ty = (barH - thumb) * equipScroll / max;
            int bx = ax + GRID_W + 3;
            g.fill(bx, ay, bx + 4, ay + barH, 0xFF333333);
            g.fill(bx, ay + ty, bx + 4, ay + ty + thumb, 0xFFAAAAAA);
        }
        return tip;
    }

    /** true — клик пришёлся на список и обработан. */
    private boolean equipClick(double mx, double my) {
        int ax = areaX(), ay = areaY();
        if (!hit(mx, my, ax, ay, GRID_W, EQ_ROWS * CELL_H)) return false;
        int n = (equipScroll + (int) ((my - ay) / CELL_H)) * COLS + (int) ((mx - ax) / CELL_W);
        if (n < 0 || n >= equipLines.size()) return true;

        EquipLine l = equipLines.get(n);
        if (l.sold()) PacketDistributor.sendToServer(EquipActionPayload.buyback(l.id()));
        else if (l.amount() > 0) PacketDistributor.sendToServer(EquipActionPayload.sell(l.key()));
        else return true;
        click();
        return true;
    }

    // =====================================================================
    //  Отрисовка
    // =====================================================================

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        refreshSellList();

        boolean dialog = dialogKey >= 0;
        int hx = dialog ? -10000 : mx;   // под диалогом наведение отключено
        int hy = dialog ? -10000 : my;

        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1E1E1E);
        g.renderOutline(x, y, imageWidth, imageHeight, 0xFF8B8B8B);
        g.drawString(font, title, x + 8, y + 5, 0xFFFFFF);

        // Валюта игрока: одна строка в шапке, выравнивание по правому краю
        int rightEdge = x + imageWidth - 8;
        rightEdge = drawBalance(g, EMERALD,
                String.valueOf(EconomyBank.total(minecraft.player, Items.EMERALD)), rightEdge, y + 3);
        rightEdge -= 10;

        drawBalance(g, new ItemStack(EcoItems.GOLD_COIN.get()),
                String.valueOf(EconomyBank.total(minecraft.player, EcoItems.GOLD_COIN.get())), rightEdge, y + 3);

        scroll = Mth.clamp(scroll, 0, maxScroll());

        if (total() == 0) {
            g.drawString(font, Component.translatable("gui.economy_core.trader.empty_buy"),
                    x + LIST_X + 4, y + LIST_Y + 6, 0xAAAAAA);
        }

        // Сетка лотов
        int hoveredLot = -1;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = (scroll + r) * COLS + c;
                if (idx >= total()) break;
                int cx = x + LIST_X + c * CELL_W;
                int cy = y + LIST_Y + r * CELL_H;
                boolean hov = hit(hx, hy, cx, cy, CELL_W, CELL_H);
                if (hov) hoveredLot = idx;
                renderCell(g, idx, cx, cy, hov);
            }
        }

        // Полоса прокрутки
        int rows = totalRows();
        if (rows > ROWS) {
            int barH = GRID_H - 2;
            int thumbH = Math.max(10, barH * ROWS / rows);
            int thumbY = (barH - thumbH) * scroll / (rows - ROWS);
            int bx = x + LIST_X + GRID_W + 3;
            g.fill(bx, y + LIST_Y, bx + 4, y + LIST_Y + barH, 0xFF333333);
            g.fill(bx, y + LIST_Y + thumbY, bx + 4, y + LIST_Y + thumbY + thumbH, 0xFFAAAAAA);
        }

        // Вкладки вместо надписи «Ваш инвентарь»
        renderTabs(g, hx, hy);

        int hoveredInv = -1;
        TraderPrices.SellLine hoveredSell = null;
        ItemStack equipTip = ItemStack.EMPTY;
        if (equipTab) {
            equipTip = renderEquipList(g, hx, hy);
        } else {
            hoveredSell = renderSellGrid(g, hx, hy);
        }

        g.drawString(font,
                font.plainSubstrByWidth(Component.translatable("gui.economy_core.trader.hint").getString(),
                        imageWidth - 16),
                x + 8, y + imageHeight - 13, 0x888888);

        if (dialog) {
            renderDialog(g, mx, my);
        } else if (!equipTip.isEmpty()) {
            g.renderTooltip(font, equipTip, mx, my);
        } else if (hoveredLot >= 0) {
            Lot l = buyLot(hoveredLot);
            if (l != null) renderLotTooltip(g, l, mx, my);
        } else if (hoveredSell != null) {
            renderLotTooltip(g, sellLot(hoveredSell), mx, my);
        }
    }

    private int drawBalance(GuiGraphics g, ItemStack icon, String text, int right, int y) {
        int textX = right - font.width(text);
        int iconX = textX - 18;
        g.renderItem(icon, iconX, y);
        g.drawString(font, text, textX, y + 4, 0xFFFFFF, true);
        return iconX;
    }

    private void renderCell(GuiGraphics g, int idx, int cx, int cy, boolean hovered) {
        Lot l = buyLot(idx);
        if (l == null) return;

        int bg = l.soldOut() ? 0xFF3A2222 : hovered ? 0xFF55555A : 0xFF333333;
        g.fill(cx, cy, cx + CELL_W - 1, cy + CELL_H - 1, bg);
        if (hovered) g.renderOutline(cx, cy, CELL_W - 1, CELL_H - 1, 0xFFFFFFFF);

        int ix = cx + (CELL_W - 1 - 16) / 2;
        int iy = cy + 3;
        g.renderItem(l.item(), ix, iy);
        g.renderItemDecorations(font, l.item(), ix, iy);

        int py = cy + 24;
        if (l.soldOut()) {
            g.drawCenteredString(font, "✘", cx + (CELL_W - 1) / 2, py, 0xFF5555);
            return;
        }

        int color = l.affordable() ? 0xFFFFFF : 0xFF5555;
        String text = String.valueOf(l.priceA().getCount());

        final float iconScale = 0.5f;
        int iconW = Math.round(16 * iconScale);
        int maxTextW = CELL_W - 1 - 2 - iconW - 1;      // отступ по 1 px с боков
        float textScale = fitScale(text, maxTextW);

        int w = costWidth(text, iconScale, textScale);
        drawCost(g, l.priceA(), text, color, cx + (CELL_W - 1 - w) / 2, py, iconScale, textScale);
    }

    /** Чем длиннее число, тем меньше шрифт. Минимум 0.4. */
    private float fitScale(String text, int maxW) {
        int w = font.width(text);
        if (w <= maxW) return 1f;
        return Math.max(0.4f, (float) Math.floor(maxW * 20f / w) / 20f);
    }

    /** Сетка продаваемого из инвентаря. Возвращает строку под курсором. */
    @Nullable
    private TraderPrices.SellLine renderSellGrid(GuiGraphics g, int mx, int my) {
        int ax = areaX(), ay = areaY(), aw = areaW();
        invScroll = Mth.clamp(invScroll, 0, sellMaxScroll());

        if (sellList.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("gui.economy_core.trader.empty_sell"),
                    ax + aw / 2, ay + 28, 0xAAAAAA);
            return null;
        }

        TraderPrices.SellLine hovered = null;
        for (int r = 0; r < EQ_ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int n = (invScroll + r) * COLS + c;
                if (n >= sellList.size()) continue;
                TraderPrices.SellLine e = sellList.get(n);
                int cx = ax + c * CELL_W, cy = ay + r * CELL_H;
                boolean hov = hit(mx, my, cx, cy, CELL_W, CELL_H);
                if (hov) hovered = e;

                g.fill(cx, cy, cx + CELL_W - 1, cy + CELL_H - 1, hov ? 0xFF55555A : 0xFF333333);
                if (hov) g.renderOutline(cx, cy, CELL_W - 1, CELL_H - 1, 0xFFFFFFFF);

                int ix = cx + (CELL_W - 1 - 16) / 2, iy = cy + 3;
                g.renderItem(e.stack(), ix, iy);
                g.renderItemDecorations(font, e.stack(), ix, iy,
                        e.have() > 1 ? bigNum(e.have()) : null);

                var q = e.quote();
                String t = String.valueOf(q.amount());
                float iconScale = 0.5f;
                int maxTextW = CELL_W - 1 - 2 - Math.round(16 * iconScale) - 1;
                float ts = fitScale(t, maxTextW);
                int w = costWidth(t, iconScale, ts);
                drawCost(g, new ItemStack(q.currency()), t, 0x55FF55,
                        cx + (CELL_W - 1 - w) / 2, cy + 24, iconScale, ts);
            }
        }

        int max = sellMaxScroll();
        if (max > 0) {
            int barH = EQ_ROWS * CELL_H - 2;
            int thumb = Math.max(10, barH * EQ_ROWS / sellRows());
            int ty = (barH - thumb) * invScroll / max;
            int bx = ax + GRID_W + 3;
            g.fill(bx, ay, bx + 4, ay + barH, 0xFF333333);
            g.fill(bx, ay + ty, bx + 4, ay + ty + thumb, 0xFFAAAAAA);
        }
        return hovered;
    }

    // ---------- Подсказка ----------

    private void renderLotTooltip(GuiGraphics g, Lot l, int mx, int my) {
        List<Component> tip = l.item().getTooltipLines(
                Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL);

        final int W = 176, PAD = 6, PREV = 40;

        Component name = tip.isEmpty() ? l.item().getHoverName() : tip.get(0);
        List<FormattedCharSequence> nameLines = font.split(name, W - PAD * 2 - PREV - 6);

        List<FormattedCharSequence> lore = new ArrayList<>();
        for (int i = 1; i < tip.size(); i++) {
            List<FormattedCharSequence> part = font.split(tip.get(i), W - PAD * 2);
            if (part.isEmpty()) lore.add(FormattedCharSequence.EMPTY);
            else lore.addAll(part);
        }

        List<Component> info = new ArrayList<>();
        if (l.sell()) {
            info.add(Component.translatable("gui.economy_core.trader.have", l.have()));
        } else if (l.soldOut()) {
            info.add(Component.translatable("gui.economy_core.trader.sold_out"));
        }

        List<ItemStack> prices = new ArrayList<>();
        prices.add(l.priceA());
        if (!l.priceB().isEmpty()) prices.add(l.priceB());

        int h = PAD + Math.max(PREV, nameLines.size() * 10) + 6
                + 11 + prices.size() * 13 + 3
                + info.size() * 10
                + (lore.isEmpty() ? 0 : 5 + lore.size() * 10) + PAD;

        int x0 = mx + 12;
        if (x0 + W > width - 2) x0 = mx - 12 - W;
        x0 = Math.max(2, x0);
        int y0 = Mth.clamp(my - 12, 2, Math.max(2, height - h - 2));

        var pose = g.pose();
        pose.pushPose();
        pose.translate(0, 0, 400);

        g.fill(x0, y0, x0 + W, y0 + h, 0xF0100010);
        g.renderOutline(x0, y0, W, h, 0xFF5B3B9E);

        g.fill(x0 + PAD, y0 + PAD, x0 + PAD + PREV, y0 + PAD + PREV, 0xFF161616);
        g.renderOutline(x0 + PAD, y0 + PAD, PREV, PREV, 0xFF3A3A3A);
        renderSpinning(g, l.item(), x0 + PAD + PREV / 2, y0 + PAD + PREV / 2, 32f);

        int tx = x0 + PAD + PREV + 6;
        int ty = y0 + PAD;
        for (FormattedCharSequence s : nameLines) {
            g.drawString(font, s, tx, ty, 0xFFFFFF);
            ty += 10;
        }

        int cy = y0 + PAD + Math.max(PREV, nameLines.size() * 10) + 6;
        g.fill(x0 + PAD, cy - 3, x0 + W - PAD, cy - 2, 0xFF555555);

        g.drawString(font, Component.translatable(l.sell()
                ? "gui.economy_core.trader.price_sell"
                : "gui.economy_core.trader.price_buy"), x0 + PAD, cy, 0xAAAAAA);
        cy += 11;
        int priceColor = l.sell() ? 0x55FF55 : 0xFFFFFF;
        for (ItemStack p : prices) {
            drawCost(g, p, String.valueOf(p.getCount()),
                    priceColor, x0 + PAD, cy, 0.75f, 1f);
            cy += 13;
        }
        cy += 3;

        for (Component c : info) {
            g.drawString(font, c, x0 + PAD, cy, 0xAAAAAA);
            cy += 10;
        }

        if (!lore.isEmpty()) {
            cy += 5;
            for (FormattedCharSequence s : lore) {
                g.drawString(font, s, x0 + PAD, cy, 0xFFFFFF);
                cy += 10;
            }
        }
        pose.popPose();
    }

    /** Блоки вращаются полностью, плоские предметы покачиваются. */
    private void renderSpinning(GuiGraphics g, ItemStack s, int cx, int cy, float size) {
        float t = (System.currentTimeMillis() % 4000L) / 4000f;

        boolean solid = minecraft.getItemRenderer()
                .getModel(s, minecraft.level, minecraft.player, 0)
                .isGui3d();
        float angle = solid ? t * 360f : 30f * Mth.sin(t * Mth.TWO_PI);

        float k = size / 16f;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 100);
        pose.scale(k, k, k);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-8, -8, -150);
        g.renderItem(s, 0, 0);
        pose.popPose();
    }

    // ---------- Диалог ----------

    private int dlgX() { return leftPos + (imageWidth - DLG_W) / 2; }
    private int dlgY() { return topPos + (imageHeight - DLG_H) / 2; }

    private void renderDialog(GuiGraphics g, int mx, int my) {
        Lot l = dialogLot();
        if (l == null) { dialogKey = -1; return; }

        int maxQ = Math.max(1, l.maxQty());
        qty = Mth.clamp(qty, 1, maxQ);
        boolean canConfirm = l.maxQty() >= 1;

        int dx = dlgX(), dy = dlgY();

        var pose = g.pose();
        pose.pushPose();
        pose.translate(0, 0, 300);

        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xB0000000);
        g.fill(dx, dy, dx + DLG_W, dy + DLG_H, 0xFF2A2A2A);
        g.renderOutline(dx, dy, DLG_W, DLG_H, 0xFFB0B0B0);

        g.drawCenteredString(font, Component.translatable(l.sell()
                ? "gui.economy_core.trader.confirm_sell"
                : "gui.economy_core.trader.confirm_buy"), dx + DLG_W / 2, dy + 6, 0xFFFFFF);

        // ---- иконка предмета + имя + количество ----
        g.fill(dx + 8, dy + 20, dx + 28, dy + 40, 0xFF161616);
        g.renderOutline(dx + 8, dy + 20, 20, 20, 0xFF3A3A3A);
        g.renderItem(l.item(), dx + 10, dy + 22);
        g.renderItemDecorations(font, l.item(), dx + 10, dy + 22);
        g.drawString(font, font.plainSubstrByWidth(l.item().getHoverName().getString(), DLG_W - 40 - 8),
                dx + 34, dy + 22, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.economy_core.trader.amount",
                        l.item().getCount() * qty),
                dx + 34, dy + 32, 0xAAAAAA);

        // ---- строка количества: − [поле] + MAX ----
        int qy = dy + 48;
        drawButton(g, dx + 16, qy, 16, 16, "−", qty > 1, mx, my, 0xFFFFFFFF);

        int fieldX = dx + 34;
        int fieldY = qy;
        int fieldW = 52;
        int fieldH = 16;
        boolean fieldHover = hit(mx, my, fieldX, fieldY, fieldW, fieldH);
        g.fill(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF161616);
        g.renderOutline(fieldX, fieldY, fieldW, fieldH,
                qtyFieldFocused ? 0xFFFFFFFF : (fieldHover ? 0xFFAAAAAA : 0xFF555555));

        String shown = qtyFieldFocused ? qtyFieldText : String.valueOf(qty);
        if (qtyFieldFocused && (System.currentTimeMillis() / 500L) % 2 == 0) {
            shown = shown + "_";
        }
        g.drawCenteredString(font, shown, fieldX + fieldW / 2, fieldY + 4,
                canConfirm ? 0xFFFFFF : 0xFF5555);

        drawButton(g, dx + 88, qy, 16, 16, "+", qty < maxQ, mx, my, 0xFFFFFFFF);
        drawButton(g, dx + 110, qy, 50, 16,
                Component.translatable("gui.economy_core.trader.max").getString(),
                qty < maxQ, mx, my, 0xFFFFFFFF);

        // ---- итог по цене ----
        int ty = dy + 70;
        Component totalLabel = Component.translatable("gui.economy_core.trader.total");
        g.drawString(font, totalLabel, dx + 16, ty + 2, 0xAAAAAA);
        int px = dx + 16 + font.width(totalLabel) + 6;
        int color = l.sell() ? 0x55FF55 : (canConfirm ? 0xFFFFFF : 0xFF5555);
        px += drawCost(g, l.priceA(), String.valueOf((long) l.priceA().getCount() * qty),
                color, px, ty, 0.75f, 1f) + 6;
        if (!l.priceB().isEmpty()) {
            drawCost(g, l.priceB(), String.valueOf((long) l.priceB().getCount() * qty),
                    color, px, ty, 0.75f, 1f);
        }

        // ---- кнопки Да / Нет ----
        int by = dy + 88;
        drawButton(g, dx + DLG_W / 2 - 44, by, 40, 16, "✔", canConfirm, mx, my, 0xFF55FF55);
        drawButton(g, dx + DLG_W / 2 + 4, by, 40, 16, "✘", true, mx, my, 0xFFFF5555);

        pose.popPose();
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h, String label,
                            boolean enabled, int mx, int my, int color) {
        boolean hov = enabled && hit(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, !enabled ? 0xFF3A3A3A : hov ? 0xFF6A6A6A : 0xFF4A4A4A);
        g.renderOutline(x, y, w, h, enabled ? color : 0xFF555555);
        g.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, enabled ? color : 0xFF777777);
    }

    // ---------- Цена: иконка + число ----------

    private int costWidth(String text, float iconScale, float textScale) {
        return Math.round(16 * iconScale) + 1 + (int) Math.ceil(font.width(text) * textScale);
    }

    private int drawCost(GuiGraphics g, ItemStack icon, String text, int color,
                         int x, int y, float iconScale, float textScale) {
        int iconW = Math.round(16 * iconScale);
        var pose = g.pose();

        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(iconScale, iconScale, 1f);
        g.renderItem(icon, 0, 0);
        pose.popPose();

        pose.pushPose();
        pose.translate(x + iconW + 1, y + (iconW - 8 * textScale) / 2f + 0.5f, 0);
        pose.scale(textScale, textScale, 1f);
        g.drawString(font, text, 0, 0, color, false);
        pose.popPose();

        return costWidth(text, iconScale, textScale);
    }

    private static String shortNum(long n) {
        if (n >= 1_000_000L) return millions(n);
        if (n >= 1_000L) return (n / 1_000L) + "k";
        return String.valueOf(n);
    }

    /** Для подсказки и диалога (места больше): сокращаем только миллионы. */
    private static String bigNum(long n) {
        return n >= 1_000_000L ? millions(n) : String.valueOf(n);
    }

    private static String millions(long n) {
        long tenths = n / 100_000L;                 // в десятых долях миллиона
        return (tenths % 10 == 0)
                ? (tenths / 10) + "M"
                : (tenths / 10) + "." + (tenths % 10) + "M";
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        // всё рисуется в render()
    }

    // =====================================================================
    //  Ввод
    // =====================================================================

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (dialogKey >= 0) {
            if (button == 0) dialogClick(mx, my);
            return true;
        }
        if (button == 0) {
            if (hit(mx, my, tabInvX(), tabY(), TAB_INV_W, TAB_H)) { selectTab(false); return true; }
            if (hit(mx, my, tabEqX(), tabY(), TAB_EQ_W, TAB_H)) { selectTab(true); return true; }
            if (equipTab && equipClick(mx, my)) return true;

            int idx = cellAt(mx, my);
            if (idx >= 0) {
                Lot l = buyLot(idx);
                if (l != null && !l.soldOut()) {
                    openDialog(idx, false);
                }
                return true;
            }
            if (!equipTab) {
                int n = sellCellAt(mx, my);
                if (n >= 0) {
                    openDialog(sellList.get(n).slot(), true);
                    return true;
                }
                if (hit(mx, my, areaX(), areaY(), GRID_W, EQ_ROWS * CELL_H)) return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
    private int sellCellAt(double mx, double my) {
        int ax = areaX(), ay = areaY();
        if (!hit(mx, my, ax, ay, GRID_W, EQ_ROWS * CELL_H)) return -1;
        int n = (invScroll + (int) ((my - ay) / CELL_H)) * COLS + (int) ((mx - ax) / CELL_W);
        return n >= 0 && n < sellList.size() ? n : -1;
    }
    private void openDialog(int key, boolean sell) {
        dialogKey = key;
        dialogSell = sell;
        qty = 1;
        qtyFieldFocused = false;
        qtyFieldText = "1";
        click();
    }

    private void dialogClick(double mx, double my) {
        Lot l = dialogLot();
        if (l == null) { dialogKey = -1; return; }

        int dx = dlgX(), dy = dlgY();
        int step = hasShiftDown() ? 10 : 1;
        int maxQ = Math.max(1, l.maxQty());
        int qy = dy + 48;
        int by = dy + 88;

        // поле ввода количества
        if (hit(mx, my, dx + 34, qy, 52, 16)) {
            qtyFieldFocused = true;
            qtyFieldText = String.valueOf(qty);
            return;
        }

        // клик мимо поля — снимаем фокус
        if (qtyFieldFocused) {
            commitQtyField(maxQ);
        }

        if (hit(mx, my, dx + 16, qy, 16, 16)) {
            qty = Mth.clamp(qty - step, 1, maxQ); click();
        } else if (hit(mx, my, dx + 88, qy, 16, 16)) {
            qty = Mth.clamp(qty + step, 1, maxQ); click();
        } else if (hit(mx, my, dx + 110, qy, 50, 16)) {
            qty = maxQ; click();
        } else if (hit(mx, my, dx + DLG_W / 2 - 44, by, 40, 16)) {
            if (l.maxQty() >= 1) confirm();
        } else if (hit(mx, my, dx + DLG_W / 2 + 4, by, 40, 16)) {
            closeDialog();
        } else if (!hit(mx, my, dx, dy, DLG_W, DLG_H)) {
            closeDialog();
        }
    }

    private void commitQtyField(int maxQ) {
        qtyFieldFocused = false;
        try {
            int v = Integer.parseInt(qtyFieldText.trim());
            qty = Mth.clamp(v, 1, maxQ);
        } catch (NumberFormatException ignored) {
            // оставляем прежнее qty
        }
        qtyFieldText = String.valueOf(qty);
    }

    private void confirm() {
        Lot l = dialogLot();
        if (l == null || l.maxQty() < 1) { dialogKey = -1; return; }
        if (qtyFieldFocused) commitQtyField(Math.max(1, l.maxQty()));
        int q = Mth.clamp(qty, 1, Math.min(999, l.maxQty()));
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                TraderMenu.encode(dialogSell, q, dialogKey));
        click();
        dialogKey = -1;
        qtyFieldFocused = false;
    }

    private void closeDialog() {
        dialogKey = -1;
        qtyFieldFocused = false;
        qtyFieldText = "";
        click();
    }

    private void click() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (dialogKey >= 0) {
            Lot l = dialogLot();
            int maxQ = l == null ? 1 : Math.max(1, l.maxQty());

            if (qtyFieldFocused) {
                if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                    commitQtyField(maxQ);
                    return true;
                }
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    qtyFieldFocused = false;
                    qtyFieldText = String.valueOf(qty);
                    return true;
                }
                if (key == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!qtyFieldText.isEmpty()) {
                        qtyFieldText = qtyFieldText.substring(0, qtyFieldText.length() - 1);
                    }
                    return true;
                }
                if (key == GLFW.GLFW_KEY_DELETE) {
                    qtyFieldText = "";
                    return true;
                }
                // Цифры обрабатывает charTyped, остальное глушим, чтобы не улетало в super.
                return true;
            }

            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) confirm();
            else if (key == GLFW.GLFW_KEY_ESCAPE) closeDialog();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (dialogKey >= 0 && qtyFieldFocused) {
            if (c >= '0' && c <= '9' && qtyFieldText.length() < QTY_FIELD_MAX_LEN) {
                if (qtyFieldText.equals("0")) qtyFieldText = "";
                qtyFieldText += c;
            }
            return true;
        }
        return super.charTyped(c, mods);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (dialogKey >= 0) {
            if (qtyFieldFocused) return true; // игнорируем
            Lot l = dialogLot();
            int maxQ = l == null ? 1 : Math.max(1, l.maxQty());
            int step = hasShiftDown() ? 10 : 1;
            qty = Mth.clamp(qty + (int) Math.signum(scrollY) * step, 1, maxQ);
            return true;
        }
        if (!equipTab && hit(mx, my, areaX(), areaY(), GRID_W + 8, EQ_ROWS * CELL_H)) {
            invScroll = Mth.clamp(invScroll - (int) Math.signum(scrollY), 0, sellMaxScroll());
            return true;
        }
        scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    private int cellAt(double mx, double my) {
        double lx = mx - leftPos - LIST_X;
        double ly = my - topPos - LIST_Y;
        if (lx < 0 || lx >= GRID_W || ly < 0 || ly >= GRID_H) return -1;
        int idx = (scroll + (int) (ly / CELL_H)) * COLS + (int) (lx / CELL_W);
        return idx < total() ? idx : -1;
    }
}