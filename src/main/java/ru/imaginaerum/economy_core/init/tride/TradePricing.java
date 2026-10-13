package ru.imaginaerum.economy_core.init.tride;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/** Цены торга без ограничения размером стека (ваниль режет до 64). */
public final class TradePricing {

    /** Ванильная формула из MerchantOffer, но без clamp по maxStackSize. */
    public static int countA(MerchantOffer o) {
        int base = o.getItemCostA().count();
        int demandBonus = Math.max(0,
                (int) Math.floor(base * (double) o.getDemand() * o.getPriceMultiplier()));
        long total = (long) base + demandBonus + o.getSpecialPriceDiff();
        return (int) Math.max(1, Math.min(total, Integer.MAX_VALUE));
    }

    public static ItemStack costA(MerchantOffer o) {
        return new ItemStack(o.getItemCostA().item().value(), countA(o));
    }

    /** Вторая цена в ванили не модифицируется. */
    public static ItemStack costB(MerchantOffer o) {
        return o.getItemCostB()
                .map(c -> new ItemStack(c.item().value(), c.count()))
                .orElse(ItemStack.EMPTY);
    }

    private TradePricing() {}
}