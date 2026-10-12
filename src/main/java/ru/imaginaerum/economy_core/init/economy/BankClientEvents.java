package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID, value = Dist.CLIENT)
public class BankClientEvents {

    @SubscribeEvent
    public static void onEmptyClick(PlayerInteractEvent.RightClickEmpty e) {
        var p = e.getEntity();

        if (e.getHand() == InteractionHand.MAIN_HAND && p.isShiftKeyDown()
                && p.getMainHandItem().isEmpty() && p.getOffhandItem().isEmpty()) {
            PacketDistributor.sendToServer(new WithdrawPayload());
        }
    }
}