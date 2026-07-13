package dev.tuna.elementbending.element;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/**
 * Sunucudaki secilebilir elementler.
 */
public enum ElementType {

    AIR("Hava Bükücü", NamedTextColor.AQUA, Material.FEATHER),
    WATER("Su Bükücü", NamedTextColor.BLUE, Material.HEART_OF_THE_SEA),
    FIRE("Ateş Bükücü", NamedTextColor.RED, Material.BLAZE_POWDER),
    EARTH("Toprak Bükücü", NamedTextColor.DARK_GREEN, Material.STONE),
    AVATAR("Avatar", NamedTextColor.LIGHT_PURPLE, Material.NETHER_STAR);

    private final String displayName;
    private final NamedTextColor color;
    private final Material icon;

    ElementType(String displayName, NamedTextColor color, Material icon) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
    }

    public String getDisplayName() {
        return dev.tuna.elementbending.util.Lang.t(displayName);
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Material getIcon() {
        return icon;
    }

    public static ElementType fromName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return ElementType.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
