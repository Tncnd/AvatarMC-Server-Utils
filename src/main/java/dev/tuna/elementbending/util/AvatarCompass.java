package dev.tuna.elementbending.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Avatar Pusulasi: yerde duran Avatar Ruhu'nu gosterir.
 * Yerde ruh yoksa bozuk pusula gibi kendi etrafinda doner.
 */
public final class AvatarCompass {

    private static final String KEY = "avatar_compass";

    private AvatarCompass() {
    }

    public static NamespacedKey key(Plugin plugin) {
        return new NamespacedKey(plugin, KEY);
    }

    public static ItemStack create(Plugin plugin) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Msg.item("Avatar Pusulası", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Msg.item("Yerde duran Avatar Ruhu'nu gösterir.", NamedTextColor.GRAY),
                Msg.item("Ruh yerde değilse iğne boşta döner.", NamedTextColor.DARK_GRAY)));
        Compat.glint(meta);
        meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isAvatarCompass(Plugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key(plugin), PersistentDataType.BYTE);
    }
}
