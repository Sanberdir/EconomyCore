package ru.imaginaerum.economy_core.init.entities.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class TraderTargetEvents {

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent e) {
        if (e.getNewAboutToBeSetTarget() instanceof EconomyTrader) {
            e.setCanceled(true);
        }
    }
}