package ru.imaginaerum.economy_core.init.economy.replace_trades;

import net.minecraft.client.Minecraft;
import java.util.List;

public final class SellEditorClient {
    public static void open(List<TradeRow> rows) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof SellEditorScreen s) s.updateRows(rows);
        else mc.setScreen(new SellEditorScreen(rows));
    }
    private SellEditorClient() {}
}