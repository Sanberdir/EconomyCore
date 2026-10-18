package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import ru.imaginaerum.economy_core.init.economy.replace_trades.ItemPickerScreen;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Окно /economycore buy: что торговцы покупают у игрока. */
public class BuyEditorScreen extends Screen {

    private static final int ROW_H = 22;
    private static final int TOP = 62;

    // колонки (смещения от x0)
    private static final int C_COUNT = 126, C_PRICE = 166, C_CUR = 214, C_XP = 236, C_DEL = 270;

    private final class Editor {
        SellRow row;
        final int y;
        @Nullable EditBox count, price, xp;
        Editor(SellRow row, int y) { this.row = row; this.y = y; }
    }

    private final List<SellRow> rows;
    private int page = 0;
    private int perPage = 8;
    private String filter = "";
    private boolean resetArmed = false, refocus = false;
    @Nullable
    private EditBox filterBox;

    private final List<Editor> editors = new ArrayList<>();

    public BuyEditorScreen(List<SellRow> rows) {
        super(Component.translatable("gui.economy_core.buy.title"));
        this.rows = new ArrayList<>(rows);
    }

    /** Ответ сервера после добавления, удаления, сброса или отклонённой правки. */
    public void updateRows(List<SellRow> fresh) {
        rows.clear();
        rows.addAll(fresh);
        resetArmed = false;
        rebuildWidgets();
    }

    private int x0() {
        return width / 2 - 145;
    }

    private List<SellRow> view() {
        String f = filter.toLowerCase(Locale.ROOT).trim();
        return rows.stream()
                .filter(r -> f.isEmpty() || r.target().toLowerCase(Locale.ROOT).contains(f))
                .toList();
    }

    private int pages(int n) {
        return Math.max(1, (n + perPage - 1) / perPage);
    }

    // ---------- виджеты ----------

    @Override
    protected void init() {
        editors.clear();

        perPage = Mth.clamp((height - TOP - 44) / ROW_H, 3, 12);
        int x0 = x0();

        if (filterBox == null) {
            filterBox = new EditBox(font, 0, 0, 150, 18, Component.empty());
            filterBox.setMaxLength(60);
            filterBox.setHint(Component.translatable("gui.economy_core.buy.search"));
            filterBox.setResponder(s -> {
                if (s.equals(filter)) return;
                commitAll();
                filter = s;
                page = 0;
                refocus = true;
                rebuildWidgets();
            });
        }
        filterBox.setPosition(x0, 34);
        addRenderableWidget(filterBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.buy.add"),
                        b -> minecraft.setScreen(new AddSellScreen(this)))
                .bounds(x0 + 156, 33, 134, 20).build());

        addRenderableWidget(Button.builder(Component.translatable(resetArmed
                        ? "gui.economy_core.sell.reset_confirm"
                        : "gui.economy_core.sell.reset"), b -> {
                    if (!resetArmed) {
                        resetArmed = true;
                        rebuildWidgets();
                        return;
                    }
                    resetArmed = false;
                    editors.clear();     // несохранённые правки отбрасываем
                    PacketDistributor.sendToServer(SellEditPayload.reset());
                })
                .bounds(width - 128, 8, 120, 20).build());

        List<SellRow> v = view();
        int pages = pages(v.size());
        page = Mth.clamp(page, 0, pages - 1);

        Button prev = Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(width / 2 - 60, height - 34, 20, 18).build();
        Button next = Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(width / 2 + 40, height - 34, 20, 18).build();
        prev.active = page > 0;
        next.active = page < pages - 1;
        addRenderableWidget(prev);
        addRenderableWidget(next);

        int y = TOP;
        for (int n = page * perPage; n < Math.min(v.size(), (page + 1) * perPage); n++) {
            SellRow r = v.get(n);
            Editor e = new Editor(r, y);
            if (!r.deny()) {
                e.count = numBox(x0 + C_COUNT, y, 36, 7, String.valueOf(r.count()));
                e.price = numBox(x0 + C_PRICE, y, 44, 7, String.valueOf(r.price()));
                e.xp = numBox(x0 + C_XP, y, 30, 4, r.xp() < 0 ? "" : String.valueOf(r.xp()));
                e.xp.setHint(Component.translatable("gui.economy_core.buy.auto"));
            }
            editors.add(e);

            final int idx = r.idx();
            addRenderableWidget(Button.builder(Component.literal("✘"), b -> {
                commitAll();
                PacketDistributor.sendToServer(SellEditPayload.delete(idx));
            }).bounds(x0 + C_DEL, y, 20, 18).build());
            y += ROW_H;
        }

        if (refocus) {
            setFocused(filterBox);
            refocus = false;
        }
    }

    private EditBox numBox(int x, int y, int w, int maxLen, String value) {
        EditBox b = new EditBox(font, x, y, w, 18, Component.empty());
        b.setMaxLength(maxLen);
        b.setFilter(s -> s.matches("\\d*"));
        b.setValue(value);
        addRenderableWidget(b);
        return b;
    }

    private void changePage(int d) {
        commitAll();
        page += d;
        rebuildWidgets();
    }

    // ---------- сохранение ----------

    private static int parse(@Nullable EditBox b, int emptyValue, int invalid) {
        if (b == null) return invalid;
        String s = b.getValue().trim();
        if (s.isEmpty()) return emptyValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return invalid;
        }
    }

    private void push(SellRow n) {
        PacketDistributor.sendToServer(SellEditPayload.set(n.idx(), n.targetId(), n.count(),
                n.price(), n.currency(), n.xp()));
        rows.replaceAll(x -> x.idx() == n.idx() ? n : x);
    }

    @Nullable
    private SellRow find(int idx) {
        for (SellRow r : rows) if (r.idx() == idx) return r;
        return null;
    }

    private void commitAll() {
        for (Editor e : editors) {
            if (e.count == null || e.price == null || e.xp == null) continue;   // запрещающие правила
            SellRow r = e.row;
            int c = parse(e.count, r.count(), r.count());
            int p = parse(e.price, r.price(), r.price());
            int x = parse(e.xp, -1, r.xp());           // пусто = авто
            if (c < 1) c = r.count();
            if (p < 1) p = r.price();
            if (c == r.count() && p == r.price() && x == r.xp()) continue;
            SellRow n = r.with(c, p, x);
            push(n);
            e.row = n;
        }
    }

    // ---------- выбор предмета кликом по иконке ----------

    private void pick(Editor e, boolean target) {
        commitAll();
        final int idx = e.row.idx();
        minecraft.setScreen(new ItemPickerScreen(this, target, id -> {
            SellRow cur = find(idx);
            if (cur == null) return;
            push(target ? cur.withTarget(id) : cur.withCurrency(id));
            rebuildWidgets();
        }));
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int x0 = x0();
            for (Editor e : editors) {
                if (hit(mx, my, x0, e.y, 18, 18)) {
                    pick(e, true);
                    return true;
                }
                if (!e.row.deny() && hit(mx, my, x0 + C_CUR, e.y, 18, 18)) {
                    pick(e, false);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void removed() {
        commitAll();      // и при закрытии, и при переходе в форму добавления
        super.removed();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
                && filterBox != null && !filterBox.isFocused()) {
            commitAll();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int pages = pages(view().size());
        int np = Mth.clamp(page - (int) Math.signum(sy), 0, pages - 1);
        if (np != page) {
            commitAll();
            page = np;
            rebuildWidgets();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------- отрисовка ----------

    private static ItemStack stack(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        Item item = loc == null ? Items.BARRIER : BuiltInRegistries.ITEM.getOptional(loc).orElse(Items.BARRIER);
        return new ItemStack(item);
    }

    private static ItemStack icon(SellRow r) {
        return switch (r.kind()) {
            case 0 -> stack(r.target());
            case 1 -> {
                ResourceLocation loc = ResourceLocation.tryParse(r.target());
                if (loc == null) yield new ItemStack(Items.BARRIER);
                List<Holder<Item>> list = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, loc))
                        .map(s -> s.stream().toList()).orElse(List.of());
                if (list.isEmpty()) yield new ItemStack(Items.BARRIER);
                int i = (int) ((System.currentTimeMillis() / 1000L) % list.size());
                yield new ItemStack(list.get(i).value());
            }
            default -> new ItemStack(Items.CHEST);
        };
    }

    private Component label(SellRow r) {
        return switch (r.kind()) {
            case 0 -> stack(r.target()).getHoverName();
            case 1 -> Component.literal("#" + r.target());
            default -> Component.translatable("gui.economy_core.buy.all");
        };
    }

    private void slot(GuiGraphics g, int x, int y, ItemStack s, boolean hov) {
        g.fill(x, y, x + 18, y + 18, 0xFF333333);
        if (hov) g.renderOutline(x, y, 18, 18, 0xFFFFFFFF);
        g.renderItem(s, x + 1, y + 1);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);

        int x0 = x0();
        g.drawString(font, Component.translatable("gui.economy_core.buy.count"), x0 + C_COUNT, TOP - 10, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.economy_core.buy.price"), x0 + C_PRICE, TOP - 10, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.economy_core.buy.xp"), x0 + C_XP, TOP - 10, 0xAAAAAA);

        ItemStack tip = ItemStack.EMPTY;
        for (Editor e : editors) {
            SellRow r = e.row;
            int y = e.y;

            ItemStack ic = icon(r);
            boolean ht = hit(mx, my, x0, y, 18, 18);
            slot(g, x0, y, ic, ht);
            if (ht && r.kind() != 2) tip = ic;

            String name = label(r).getString() + (r.complex() ? " ✦" : "");
            g.drawString(font, font.plainSubstrByWidth(name, 98), x0 + 22, y + 5,
                    r.deny() ? 0xFF5555 : 0xFFFFFF);

            if (r.deny()) {
                g.drawString(font, Component.translatable("gui.economy_core.buy.denied"),
                        x0 + C_COUNT, y + 5, 0xFF5555);
            } else {
                ItemStack cur = stack(r.currency());
                boolean hc = hit(mx, my, x0 + C_CUR, y, 18, 18);
                slot(g, x0 + C_CUR, y, cur, hc);
                if (hc) tip = cur;
            }
        }

        int total = view().size();
        if (total == 0) {
            g.drawCenteredString(font, Component.translatable("gui.economy_core.buy.empty"),
                    width / 2, TOP + 10, 0xAAAAAA);
        }
        g.drawCenteredString(font, (page + 1) + " / " + pages(total), width / 2, height - 30, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("gui.economy_core.buy.hint"),
                width / 2, height - 12, 0x888888);

        if (!tip.isEmpty()) g.renderTooltip(font, tip, mx, my);
    }
}