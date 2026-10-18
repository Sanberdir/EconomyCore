package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.client.Minecraft;

import java.util.List;

public final class BuyEditorClient {
    public static void open(List<SellRow> rows) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BuyEditorScreen s) s.updateRows(rows);
        else mc.setScreen(new BuyEditorScreen(rows));
    }
    private BuyEditorClient() {}
}