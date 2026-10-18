package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SellEditorScreen extends Screen {

    private static final int ROW_H = 24;
    private static final int TOP = 70;

    /** Поля одной строки торга текущего уровня. */
    private final class Editor {
        TradeRow row;
        EditBox resCount, priceCount, uses, xp;
        Editor(TradeRow row) { this.row = row; }
    }

    private final List<TradeRow> rows;
    @Nullable
    private String profession;
    private int level = 1;
    private boolean resetArmed = false;
    private final List<Editor> editors = new ArrayList<>();

    public SellEditorScreen(List<TradeRow> rows) {
        super(Component.translatable("gui.economy_core.sell.title"));
        this.rows = new ArrayList<>(rows);
    }

    /** Ответ сервера после добавления, удаления или сброса. */
    public void updateRows(List<TradeRow> fresh) {
        rows.clear();
        rows.addAll(fresh);
        resetArmed = false;
        rebuildWidgets();
    }

    private static Component profName(String p) {
        return Component.translatable("entity.minecraft.villager." + p);
    }

    // ---------- Виджеты ----------

    @Override
    protected void init() {
        editors.clear();
        if (profession == null) initProfessions();
        else initTrades();
    }

    private void initProfessions() {
        List<String> profs = rows.stream().map(TradeRow::profession).distinct().sorted().toList();
        int cols = 4, w = 100, h = 20, gap = 4;
        int x0 = (width - (cols * w + (cols - 1) * gap)) / 2;
        for (int i = 0; i < profs.size(); i++) {
            String p = profs.get(i);
            addRenderableWidget(Button.builder(profName(p), b -> {
                        profession = p;
                        level = 1;
                        resetArmed = false;
                        rebuildWidgets();
                    })
                    .bounds(x0 + (i % cols) * (w + gap), 40 + (i / cols) * (h + gap), w, h)
                    .build());
        }
    }

    private EditBox numBox(int x, int y, int w, int maxLen, int value, boolean editable) {
        EditBox b = new EditBox(font, x, y, w, 18, Component.empty());
        b.setMaxLength(maxLen);
        b.setFilter(s -> s.matches("\\d*"));
        b.setValue(String.valueOf(value));
        b.setEditable(editable);
        addRenderableWidget(b);
        return b;
    }

    private void initTrades() {
        addRenderableWidget(Button.builder(Component.literal("←"), b -> {
            commitAll();
            profession = null;
            resetArmed = false;
            rebuildWidgets();
        }).bounds(8, 8, 24, 20).build());

        addRenderableWidget(Button.builder(Component.translatable(resetArmed
                        ? "gui.economy_core.sell.reset_confirm"
                        : "gui.economy_core.sell.reset"), b -> {
                    if (!resetArmed) {
                        resetArmed = true;
                        rebuildWidgets();
                        return;
                    }
                    resetArmed = false;
                    editors.clear();          // несохранённые правки отбрасываем
                    PacketDistributor.sendToServer(TradeEditPayload.reset(profession));
                })
                .bounds(width - 128, 8, 120, 20).build());

        int tabW = 28, gap = 4;
        int tx = (width - (5 * tabW + 4 * gap)) / 2;
        for (int l = 1; l <= 5; l++) {
            final int lv = l;
            Button tab = Button.builder(Component.literal(String.valueOf(l)), b -> {
                        commitAll();
                        level = lv;
                        resetArmed = false;
                        rebuildWidgets();
                    })
                    .bounds(tx + (l - 1) * (tabW + gap), 34, tabW, 20).build();
            tab.active = l != level;
            addRenderableWidget(tab);
        }

        int x0 = width / 2 - 130;
        int y = TOP;
        for (TradeRow r : rows) {
            if (!r.profession().equals(profession) || r.level() != level) continue;

            Editor e = new Editor(r);
            e.resCount = numBox(x0 + 22, y, 44, 7, r.resultCount(), r.resultEditable());
            e.priceCount = numBox(x0 + 104, y, 56, 7, r.priceCount(), true);
            e.uses = numBox(x0 + 164, y, 36, 4, r.maxUses(), true);
            e.xp = numBox(x0 + 204, y, 30, 4, r.xp(), true);
            editors.add(e);

            final int i = editors.size() - 1;
            addRenderableWidget(Button.builder(Component.literal("✘"), b -> {
                commitAll();
                TradeRow cur = editors.get(i).row;
                PacketDistributor.sendToServer(TradeEditPayload.delete(cur.profession(), cur.level(), cur.idx()));
            }).bounds(x0 + 238, y, 20, 18).build());

            y += ROW_H;
        }

        if (editors.size() < TradeRow.MAX_PER_LEVEL) {
            addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.sell.add"),
                            b -> minecraft.setScreen(new AddTradeScreen(this, profession, level)))
                    .bounds(width / 2 - 70, y + 4, 140, 20).build());
        }
    }

    // ---------- Сохранение ----------

    private static int parse(EditBox b) {
        try {
            return Integer.parseInt(b.getValue().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Некорректное значение (пусто, меньше минимума) заменяется прежним. */
    private static int or(int v, int min, int old) {
        return v >= min ? v : old;
    }

    private void push(TradeRow n) {
        PacketDistributor.sendToServer(TradeEditPayload.edit(n.profession(), n.level(), n.idx(),
                n.resultId(), n.resultCount(), n.priceId(), n.priceCount(), n.maxUses(), n.xp()));
        rows.replaceAll(x -> sameKey(x, n) ? n : x);
    }

    private static boolean sameKey(TradeRow a, TradeRow b) {
        return a.profession().equals(b.profession()) && a.level() == b.level() && a.idx() == b.idx();
    }

    @Nullable
    private TradeRow find(String prof, int lvl, int idx) {
        for (TradeRow r : rows) {
            if (r.profession().equals(prof) && r.level() == lvl && r.idx() == idx) return r;
        }
        return null;
    }

    private void commitAll() {
        for (Editor e : editors) {
            TradeRow r = e.row;
            int rc = r.resultEditable() ? or(parse(e.resCount), 1, r.resultCount()) : r.resultCount();
            int pc = or(parse(e.priceCount), 1, r.priceCount());
            int mu = or(parse(e.uses), 1, r.maxUses());
            int xp = or(parse(e.xp), 0, r.xp());
            if (rc == r.resultCount() && pc == r.priceCount() && mu == r.maxUses() && xp == r.xp()) continue;
            TradeRow n = r.with(rc, pc, mu, xp);
            push(n);
            e.row = n;
        }
    }

    // ---------- Выбор предмета кликом по иконке ----------

    private void pick(Editor e, boolean result) {
        commitAll();
        final String prof = e.row.profession();
        final int lv = e.row.level(), ix = e.row.idx();
        minecraft.setScreen(new ItemPickerScreen(this, false, id -> {
            TradeRow cur = find(prof, lv, ix);
            if (cur == null) return;
            push(result ? cur.withResultId(id) : cur.withPriceId(id));
            rebuildWidgets();
        }));
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (profession != null && button == 0) {
            int x0 = width / 2 - 130;
            int y = TOP;
            for (Editor e : editors) {
                if (e.row.resultEditable() && hit(mx, my, x0, y, 18, 18)) {
                    pick(e, true);
                    return true;
                }
                if (hit(mx, my, x0 + 82, y, 18, 18)) {
                    pick(e, false);
                    return true;
                }
                y += ROW_H;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void removed() {
        commitAll();      // вызывается и при переходе в форму добавления или выбор предмета
        super.removed();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (profession != null && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            commitAll();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;   // чтобы встроенный сервер не стоял и пакеты доходили
    }

    // ---------- Отрисовка ----------

    private static ItemStack stack(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        Item item = loc == null ? Items.BARRIER : BuiltInRegistries.ITEM.getOptional(loc).orElse(Items.BARRIER);
        return new ItemStack(item);
    }

    private void slot(GuiGraphics g, int x, int y, ItemStack s, boolean clickable, boolean hov) {
        g.fill(x, y, x + 18, y + 18, 0xFF333333);
        if (hov && clickable) g.renderOutline(x, y, 18, 18, 0xFFFFFFFF);
        g.renderItem(s, x + 1, y + 1);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, profession == null ? title : profName(profession), width / 2, 14, 0xFFFFFF);

        if (profession == null) {
            if (rows.isEmpty()) {
                g.drawCenteredString(font, Component.translatable("gui.economy_core.sell.empty"),
                        width / 2, 60, 0xAAAAAA);
            }
            return;
        }

        int x0 = width / 2 - 130;
        g.drawString(font, Component.translatable("gui.economy_core.sell.col.result"), x0, TOP - 11, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.economy_core.sell.col.price"), x0 + 82, TOP - 11, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.economy_core.sell.col.uses"), x0 + 164, TOP - 11, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.economy_core.sell.col.xp"), x0 + 204, TOP - 11, 0xAAAAAA);

        ItemStack tip = ItemStack.EMPTY;
        int y = TOP;
        for (Editor e : editors) {
            TradeRow r = e.row;

            ItemStack res = stack(r.resultId());
            boolean hr = hit(mx, my, x0, y, 18, 18);
            slot(g, x0, y, res, r.resultEditable(), hr);
            if (hr) tip = res;

            g.drawString(font, "→", x0 + 70, y + 5, 0xAAAAAA);

            ItemStack pr = stack(r.priceId());
            boolean hp = hit(mx, my, x0 + 82, y, 18, 18);
            slot(g, x0 + 82, y, pr, true, hp);
            if (hp) tip = pr;

            y += ROW_H;
        }

        g.drawCenteredString(font, Component.translatable("gui.economy_core.sell.hint"),
                width / 2, height - 16, 0x888888);

        if (!tip.isEmpty()) g.renderTooltip(font, tip, mx, my);
    }
}