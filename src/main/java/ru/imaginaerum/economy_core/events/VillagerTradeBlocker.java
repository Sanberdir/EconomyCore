package ru.imaginaerum.economy_core.events;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.entities.entity.EconomyTrader;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class VillagerTradeBlocker {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        System.out.println("[Blocker] EntityInteract target=" + event.getTarget().getClass().getSimpleName());
        if (isBlocked(event.getTarget())) {
            System.out.println("[Blocker] CANCELLED");
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isBlocked(event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static boolean isBlocked(Entity target) {
        if (target instanceof EconomyTrader) return false;
        return target instanceof AbstractVillager;
    }
}