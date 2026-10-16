package ru.imaginaerum.economy_core.init.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import ru.imaginaerum.economy_core.EconomyCore;
import ru.imaginaerum.economy_core.init.economy.EconomyAttachments;

import java.util.HashMap;
import java.util.Map;

/**
 * Торги жителя фиксируются за профессией: случайно они генерируются только в первый раз.
 * Если убрать блок профессии и поставить снова, вернутся те же торги.
 */
@EventBusSubscriber(modid = EconomyCore.MODID)
public class VillagerTradeLockEvents {

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof Villager v) || v.level().isClientSide) return;

        VillagerProfession prof = v.getVillagerData().getProfession();
        String key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(prof).toString();
        String last = v.getData(EconomyAttachments.LAST_PROFESSION.get());

        if (!key.equals(last)) {
            v.setData(EconomyAttachments.LAST_PROFESSION.get(), key);

            if (prof != VillagerProfession.NONE && prof != VillagerProfession.NITWIT) {
                MerchantOffers saved = v.getData(EconomyAttachments.TRADE_MEMORY.get()).get(key);
                if (saved != null) {
                    v.overrideOffers(saved);      // возвращаем прежние торги
                } else {
                    remember(v, key);             // первый раз: фиксируем то, что сгенерировалось
                }
            }
            return;
        }

        // Периодически обновляем снимок (прокачка, использования, новые торги уровня)
        if (v.tickCount % 20 == 0
                && prof != VillagerProfession.NONE && prof != VillagerProfession.NITWIT) {
            remember(v, key);
        }
    }

    private static void remember(Villager v, String key) {
        MerchantOffers offers = v.getOffers();   // сгенерирует, если ещё нет
        Map<String, MerchantOffers> mem = v.getData(EconomyAttachments.TRADE_MEMORY.get());
        if (mem.get(key) == offers) return;
        Map<String, MerchantOffers> copy = new HashMap<>(mem);
        copy.put(key, offers);
        v.setData(EconomyAttachments.TRADE_MEMORY.get(), copy);
    }
}