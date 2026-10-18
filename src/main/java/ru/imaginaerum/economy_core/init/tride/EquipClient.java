package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.client.Minecraft;

import java.util.List;

public final class EquipClient {
    public static void receive(List<EquipLine> lines) {
        if (Minecraft.getInstance().screen instanceof TraderScreen s) s.setEquipLines(lines);
    }
    private EquipClient() {}
}