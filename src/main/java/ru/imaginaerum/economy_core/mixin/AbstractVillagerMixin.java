package ru.imaginaerum.economy_core.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    /**
     * Выбор торгов жителя детерминирован: seed = UUID жителя + профессия + уровень.
     * Сколько раз ни генерируй, получится тот же набор.
     */
    @Inject(method = "addOffersFromItemListings", at = @At("HEAD"), cancellable = true)
    private void economy$seeded(MerchantOffers offers, VillagerTrades.ItemListing[] listings,
                                int max, CallbackInfo ci) {
        if (!((Object) this instanceof Villager v)) return;

        var data = v.getVillagerData();
        long seed = v.getUUID().getMostSignificantBits()
                ^ Long.rotateLeft(v.getUUID().getLeastSignificantBits(), 17)
                ^ ((long) BuiltInRegistries.VILLAGER_PROFESSION.getKey(data.getProfession())
                .toString().hashCode() * 31L)
                ^ ((long) data.getLevel() * 0x9E3779B97F4A7C15L);
        RandomSource rnd = RandomSource.create(seed);

        // копия ванильной логики, но со своим рандомом
        ArrayList<VillagerTrades.ItemListing> list = new ArrayList<>(java.util.List.of(listings));
        int added = 0;
        while (added < max && !list.isEmpty()) {
            MerchantOffer offer = list.remove(rnd.nextInt(list.size())).getOffer(v, rnd);
            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
        ci.cancel();
    }
}