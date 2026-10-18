package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Выбор предмета из сетки с поиском. special = true добавляет режим тегов и кнопку «*».
 * Результат: "namespace:item", "#namespace:tag" или "*". Экран возвращается к parent сам.
 */
public class ItemPickerScreen extends Screen {

    private static final int COLS = 12, CELL = 20, TOP = 58, TAG_H = 18;

    private record Entry(ItemStack stack, String id, String name) {}

    private static List<ItemStack> STACKS;

    private final Screen parent;
    private final boolean special;
    private final Consumer<String> onPick;

    private final List<Entry> items = new ArrayList<>();
    private final List<String> tagIds = new ArrayList<>();
    private List<Entry> shownItems = List.of();
    private List<String> shownTags = List.of();

    private boolean tagMode = false;
    private String filter = "";
    private int scroll = 0;
    private EditBox search;

    public ItemPickerScreen(Screen parent, boolean special, Consumer<String> onPick) {
        super(Component.translatable("gui.economy_core.pick.title"));
        this.parent = parent;
        this.special = special;
        this.onPick = onPick;

        if (STACKS == null) {
            STACKS = BuiltInRegistries.ITEM.stream()
                    .filter(i -> i != Items.AIR)
                    .map(i -> new ItemStack(i))
                    .toList();
        }
        for (ItemStack s : STACKS) {
            items.add(new Entry(s, BuiltInRegistries.ITEM.getKey(s.getItem()).toString(),
                    s.getHoverName().getString().toLowerCase(Locale.ROOT)));
        }
        if (special) {
            BuiltInRegistries.ITEM.getTagNames()
                    .map(t -> t.location().toString())
                    .sorted()
                    .forEach(tagIds::add);
        }
        refilter();
    }

    private void refilter() {
        final String f = filter.toLowerCase(Locale.ROOT).trim();
        shownItems = items.stream()
                .filter(e -> f.isEmpty() || e.id().contains(f) || e.name().contains(f))
                .toList();
        shownTags = tagIds.stream()
                .filter(t -> f.isEmpty() || t.contains(f))
                .toList();
        scroll = 0;
    }

    // ---------- виджеты ----------

    @Override
    protected void init() {
        int x0 = width / 2 - COLS * CELL / 2;

        addRenderableWidget(Button.builder(Component.literal("←"), b -> onClose())
                .bounds(8, 8, 24, 20).build());

        search = new EditBox(font, x0, 32, special ? 120 : COLS * CELL, 18, Component.empty());
        search.setMaxLength(60);
        search.setHint(Component.translatable("gui.economy_core.pick.search"));
        search.setValue(filter);
        search.setResponder(s -> {
            filter = s;
            refilter();
        });
        addRenderableWidget(search);
        setInitialFocus(search);

        if (special) {
            addRenderableWidget(Button.builder(Component.translatable(tagMode
                            ? "gui.economy_core.pick.items"
                            : "gui.economy_core.pick.tags"), b -> {
                        tagMode = !tagMode;
                        scroll = 0;
                        rebuildWidgets();
                    })
                    .bounds(x0 + 124, 31, 56, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.economy_core.pick.any"),
                            b -> choose("*"))
                    .bounds(x0 + 184, 31, 56, 20).build());
        }
    }

    private void choose(String id) {
        minecraft.setScreen(parent);   // сначала возвращаемся, потом отдаём результат
        onPick.accept(id);
    }

    // ---------- геометрия ----------

    private int rowsVisible() {
        return Math.max(1, (height - TOP - 22) / CELL);
    }

    private int tagRowsVisible() {
        return Math.max(1, (height - TOP - 22) / TAG_H);
    }

    private int totalRows() {
        return tagMode ? shownTags.size() : (shownItems.size() + COLS - 1) / COLS;
    }

    private int visibleRows() {
        return tagMode ? tagRowsVisible() : rowsVisible();
    }

    private int maxScroll() {
        return Math.max(0, totalRows() - visibleRows());
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------- отрисовка ----------

    private ItemStack tagIcon(String t) {
        ResourceLocation loc = ResourceLocation.tryParse(t);
        if (loc == null) return new ItemStack(Items.BARRIER);
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, loc))
                .filter(s -> s.size() > 0)
                .map(s -> new ItemStack(s.get((int) ((System.currentTimeMillis() / 1000L) % s.size())).value()))
                .orElseGet(() -> new ItemStack(Items.BARRIER));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);

        scroll = Mth.clamp(scroll, 0, maxScroll());
        int x0 = width / 2 - COLS * CELL / 2;
        int vis = visibleRows();

        ItemStack tip = ItemStack.EMPTY;
        String hoverId = null;

        if (tagMode) {
            for (int r = 0; r < vis; r++) {
                int n = scroll + r;
                if (n >= shownTags.size()) break;
                int y = TOP + r * TAG_H;
                boolean hov = hit(mx, my, x0, y, COLS * CELL, TAG_H - 1);
                g.fill(x0, y, x0 + COLS * CELL, y + TAG_H - 1, hov ? 0xFF55555A : 0xFF333333);
                String t = shownTags.get(n);
                g.renderItem(tagIcon(t), x0 + 1, y);
                g.drawString(font, font.plainSubstrByWidth("#" + t, COLS * CELL - 24),
                        x0 + 22, y + 5, 0xFFFFFF);
            }
        } else {
            for (int r = 0; r < vis; r++) {
                for (int c = 0; c < COLS; c++) {
                    int n = (scroll + r) * COLS + c;
                    if (n >= shownItems.size()) break;
                    int x = x0 + c * CELL, y = TOP + r * CELL;
                    boolean hov = hit(mx, my, x, y, CELL, CELL);
                    g.fill(x, y, x + CELL - 1, y + CELL - 1, hov ? 0xFF55555A : 0xFF333333);
                    Entry e = shownItems.get(n);
                    g.renderItem(e.stack(), x + 1, y + 1);
                    if (hov) {
                        g.renderOutline(x, y, CELL - 1, CELL - 1, 0xFFFFFFFF);
                        tip = e.stack();
                        hoverId = e.id();
                    }
                }
            }
        }

        if (totalRows() == 0) {
            g.drawCenteredString(font, Component.translatable("gui.economy_core.pick.empty"),
                    width / 2, TOP + 10, 0xAAAAAA);
        }

        int ms = maxScroll();
        if (ms > 0) {
            int h = vis * (tagMode ? TAG_H : CELL);
            int thumb = Math.max(10, h * vis / totalRows());
            int ty = (h - thumb) * scroll / ms;
            int bx = x0 + COLS * CELL + 3;
            g.fill(bx, TOP, bx + 4, TOP + h, 0xFF333333);
            g.fill(bx, TOP + ty, bx + 4, TOP + ty + thumb, 0xFFAAAAAA);
        }

        if (hoverId != null) {
            g.drawCenteredString(font, hoverId, width / 2, height - 14, 0x888888);
        }
        if (!tip.isEmpty()) {
            g.renderTooltip(font, tip, mx, my);
        }
    }

    // ---------- ввод ----------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int x0 = width / 2 - COLS * CELL / 2;
            if (tagMode) {
                if (mx >= x0 && mx < x0 + COLS * CELL && my >= TOP) {
                    int r = (int) ((my - TOP) / TAG_H);
                    int n = scroll + r;
                    if (r < tagRowsVisible() && n < shownTags.size()) {
                        choose("#" + shownTags.get(n));
                        return true;
                    }
                }
            } else {
                double lx = mx - x0, ly = my - TOP;
                if (lx >= 0 && lx < COLS * CELL && ly >= 0) {
                    int r = (int) (ly / CELL), c = (int) (lx / CELL);
                    int n = (scroll + r) * COLS + c;
                    if (r < rowsVisible() && n < shownItems.size()) {
                        choose(shownItems.get(n).id());
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int step = hasShiftDown() ? 5 : 1;
        scroll = Mth.clamp(scroll - (int) Math.signum(sy) * step, 0, maxScroll());
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        // Enter выбирает единственный найденный предмет
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
                && !tagMode && shownItems.size() == 1) {
            choose(shownItems.get(0).id());
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}