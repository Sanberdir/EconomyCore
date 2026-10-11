package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import ru.imaginaerum.economy_core.EconomyCore;

@EventBusSubscriber(modid = EconomyCore.MODID)
public class BankEvents {

    /** Shift + ПКМ изумрудами по воздуху. */
    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem e) {
        Player p = e.getEntity();
        if (p.level().isClientSide || !p.isShiftKeyDown()) return;
        ItemStack s = e.getItemStack();
        if (!s.is(Items.EMERALD)) return;

        EconomyBank.deposit(p, s);
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
    }

    /** Shift + ПКМ по блоку: изумруды вносим, пустой рукой снимаем. */
    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock e) {
        Player p = e.getEntity();
        if (p.level().isClientSide || !p.isShiftKeyDown()) return;

        ItemStack s = e.getItemStack();
        if (s.is(Items.EMERALD)) {
            EconomyBank.deposit(p, s);
        } else if (e.getHand() == InteractionHand.MAIN_HAND
                && s.isEmpty() && p.getOffhandItem().isEmpty()) {
            EconomyBank.withdraw(p);
        } else {
            return;
        }
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
    }
}