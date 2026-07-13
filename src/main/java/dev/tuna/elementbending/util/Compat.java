package dev.tuna.elementbending.util;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

/**
 * 1.20.x - 1.21.x uyumluluk katmani.
 *
 * 1.20.5 ile PotionEffectType/Particle isimleri degisti (SLOW -> SLOWNESS vb.),
 * MACE/Wind Charge gibi icerikler ise yalnizca yeni surumlerde var.
 * Derleme 1.21 API'sine karsi yapilir; calisma zamaninda isimler
 * alan (field) uzerinden cozulur ki ayni jar tum surumlerde calissin.
 */
public final class Compat {

    // --- Potion efektleri (yeni isim, eski isim) ---
    public static final PotionEffectType SLOWNESS = effect("SLOWNESS", "SLOW");
    public static final PotionEffectType HASTE = effect("HASTE", "FAST_DIGGING");
    public static final PotionEffectType RESISTANCE = effect("RESISTANCE", "DAMAGE_RESISTANCE");
    public static final PotionEffectType MINING_FATIGUE = effect("MINING_FATIGUE", "SLOW_DIGGING");

    // --- Partiküller (1.20.5'te yeniden adlandirildi) ---
    public static final Particle PARTICLE_SPLASH = particle("SPLASH", "WATER_SPLASH");
    public static final Particle PARTICLE_BLOCK = particle("BLOCK", "BLOCK_CRACK");
    /** GUST eski surumlerde yok; bulut ile idare edilir. */
    public static final Particle PARTICLE_GUST = particle("GUST", "CLOUD");

    // --- Sesler (Wind Charge sesleri 1.20.3+) ---
    public static final Sound SOUND_WIND_BURST = sound("ENTITY_WIND_CHARGE_WIND_BURST", "ENTITY_ENDER_DRAGON_FLAP");
    public static final Sound SOUND_WIND_THROW = sound("ENTITY_WIND_CHARGE_THROW", "ENTITY_SNOWBALL_THROW");

    /** MACE 1.21+; eski surumlerde null olur ve ilgili ozellikler atlanir. */
    public static final Material MACE = Material.matchMaterial("MACE");

    private Compat() {
    }

    /** Verilen isimlerden ilk mevcut materyali dondurur (GUI ikonlari icin). */
    public static Material material(String... names) {
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                return material;
            }
        }
        return Material.BARRIER;
    }

    /**
     * Wind Charge firlatir; entity eski surumde yoksa kartopu ile
     * benzer bir his verilir.
     */
    @SuppressWarnings("unchecked")
    public static void launchWindCharge(Player player) {
        try {
            Class<?> windChargeClass = Class.forName("org.bukkit.entity.WindCharge");
            player.launchProjectile((Class<? extends Projectile>) windChargeClass);
        } catch (ClassNotFoundException ex) {
            player.launchProjectile(Snowball.class,
                    player.getEyeLocation().getDirection().multiply(1.4));
        }
    }

    /** Parlama efekti; eski surumde buyu + gizli buyu bayragi ile taklit edilir. */
    public static void glint(ItemMeta meta) {
        try {
            meta.setEnchantmentGlintOverride(true);
        } catch (NoSuchMethodError ex) {
            // Eski surum: getByKey kesinlikle mevcut; derleme bagimliligi
            // olmamasi icin yansima ile cagrilir.
            try {
                Object unbreaking = org.bukkit.enchantments.Enchantment.class
                        .getMethod("getByKey", org.bukkit.NamespacedKey.class)
                        .invoke(null, org.bukkit.NamespacedKey.minecraft("unbreaking"));
                if (unbreaking != null) {
                    meta.addEnchant((org.bukkit.enchantments.Enchantment) unbreaking, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                }
            } catch (Throwable ignored) {
                // parlama olmadan devam
            }
        }
    }

    private static PotionEffectType effect(String... fieldNames) {
        for (String name : fieldNames) {
            try {
                return (PotionEffectType) PotionEffectType.class.getField(name).get(null);
            } catch (ReflectiveOperationException ignored) {
                // sonraki ismi dene
            }
        }
        throw new IllegalStateException("PotionEffectType bulunamadi: " + String.join("/", fieldNames));
    }

    private static Particle particle(String... names) {
        for (String name : names) {
            try {
                return Particle.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // sonraki ismi dene
            }
        }
        throw new IllegalStateException("Particle bulunamadi: " + String.join("/", names));
    }

    private static Sound sound(String... names) {
        for (String name : names) {
            try {
                // Sound 1.21.3'te enum'dan interface'e cevrildi; alan erisimi ikisinde de calisir
                return (Sound) Sound.class.getField(name).get(null);
            } catch (ReflectiveOperationException ignored) {
                // sonraki ismi dene
            }
        }
        throw new IllegalStateException("Sound bulunamadi: " + String.join("/", names));
    }
}
