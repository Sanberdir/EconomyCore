package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Работа со слотами Curios. Ключ слота: "<id слота>:<номер>", например "ring:0". */
final class CuriosCompat {

    static List<String> keys(ServerPlayer p) {
        List<String> out = new ArrayList<>();
        CuriosApi.getCuriosInventory(p).ifPresent(inv -> inv.getCurios().forEach((id, h) -> {
            IDynamicStackHandler st = h.getStacks();
            for (int i = 0; i < st.getSlots(); i++) {
                if (!st.getStackInSlot(i).isEmpty()) out.add(id + ":" + i);
            }
        }));
        return out;
    }

    @Nullable
    private static IDynamicStackHandler stacks(ServerPlayer p, String id) {
        return CuriosApi.getCuriosInventory(p)
                .map(inv -> inv.getCurios().get(id))
                .map(ICurioStacksHandler::getStacks)
                .orElse(null);
    }

    static ItemStack get(ServerPlayer p, String key) {
        int c = key.lastIndexOf(':');
        if (c < 0) return ItemStack.EMPTY;
        IDynamicStackHandler st = stacks(p, key.substring(0, c));
        int i = parse(key.substring(c + 1));
        return st == null || i < 0 || i >= st.getSlots() ? ItemStack.EMPTY : st.getStackInSlot(i);
    }

    static void set(ServerPlayer p, String key, ItemStack s) {
        int c = key.lastIndexOf(':');
        if (c < 0) return;
        IDynamicStackHandler st = stacks(p, key.substring(0, c));
        int i = parse(key.substring(c + 1));
        if (st != null && i >= 0 && i < st.getSlots()) st.setStackInSlot(i, s);
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private CuriosCompat() {}
}