package dev.tuna.elementbending.ability;

import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.Compat;
import org.bukkit.Material;

/**
 * Tum guclerin kimligi, varsayilan bekleme suresi ve GUI bilgileri.
 */
public enum AbilityKey {

    AIR_JUMP(ElementType.AIR, "Hava Sıçrayışı", "Seni havaya fırlatır.", 20, Material.FEATHER),
    AIR_BLAST(ElementType.AIR, "Hava Patlaması", "Yakındaki tüm canlıları geri iter.", 30, Compat.material("WIND_CHARGE", "SNOWBALL")),
    TORNADO(ElementType.AIR, "Kasırga", "Yakındaki tüm canlıları havaya savurur, oyuncuların ağır silahlarını devre dışı bırakır.", 180, Compat.material("BREEZE_ROD", "BLAZE_ROD")),

    FLAME_WAVE(ElementType.FIRE, "Alev Dalgası", "Önündeki tüm canlıları ateşe verir.", 20, Material.BLAZE_POWDER),
    FIRE_DASH(ElementType.FIRE, "Ateş Sıçrayışı", "Ateş/lav üzerindeyken kısa süre Speed II verir.", 30, Material.MAGMA_CREAM),
    DRAGON_FIRE(ElementType.FIRE, "Ejderha Alevi", "15 sn boyunca Ghast Fireball fırlatırsın, etrafın alev alır.", 180, Material.FIRE_CHARGE),

    WATER_PUSH(ElementType.WATER, "Su İtişi", "Önündeki tüm canlıları su kuvvetiyle iter.", 20, Material.WATER_BUCKET),
    ICE_CHAIN(ElementType.WATER, "Buz Zinciri", "Baktığın canlıyı kısa süre dondurur.", 30, Material.PACKED_ICE),
    HEALING_WAVE(ElementType.WATER, "Şifa Dalgası", "Seni ve yakın takım arkadaşlarını iyileştirir.", 180, Material.HEART_OF_THE_SEA),

    ROCK_PUNCH(ElementType.EARTH, "Kaya Yumruğu", "Sonraki yakın dövüş vuruşun ekstra hasar verir.", 20, Material.STONE),
    STONE_WALL(ElementType.EARTH, "Taş Duvar", "Önünde kısa süreli taş duvar oluşturur.", 30, Material.COBBLESTONE),
    EARTHQUAKE(ElementType.EARTH, "Deprem", "Yakındakilere Slowness III uygular ve onları sarsar.", 180, Material.POINTED_DRIPSTONE),

    AVATAR_STATE(ElementType.AVATAR, "Avatar State", "15 sn boyunca güçlenirsin, ardından kısa süre zayıflarsın.", 300, Material.NETHER_STAR);

    private final ElementType element;
    private final String displayName;
    private final String description;
    private final int defaultCooldownSeconds;
    private final Material icon;

    AbilityKey(ElementType element, String displayName, String description, int defaultCooldownSeconds, Material icon) {
        this.element = element;
        this.displayName = displayName;
        this.description = description;
        this.defaultCooldownSeconds = defaultCooldownSeconds;
        this.icon = icon;
    }

    public ElementType getElement() {
        return element;
    }

    public String getDisplayName() {
        return dev.tuna.elementbending.util.Lang.t(displayName);
    }

    public String getDescription() {
        return dev.tuna.elementbending.util.Lang.t(description);
    }

    public int getDefaultCooldownSeconds() {
        return defaultCooldownSeconds;
    }

    public Material getIcon() {
        return icon;
    }

    public static AbilityKey fromName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return AbilityKey.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
