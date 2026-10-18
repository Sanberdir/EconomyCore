package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.client.Minecraft;
import ru.imaginaerum.economy_core.init.tride.SyncOffersPayload;
import ru.imaginaerum.economy_core.init.tride.TraderMenu;

public final class ClientOfferSync {
    public static void handle(SyncOffersPayload p) {
        var mc = Minecraft.getInstance();
        if (mc.player != null
                && mc.player.containerMenu instanceof TraderMenu m
                && m.containerId == p.containerId()) {
            m.replaceOffers(p.offers());
        }
    }
    private ClientOfferSync() {}
}