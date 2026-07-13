package dev.tuna.elementbending.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Craft ile elde edilen "Avatar Ruhu" itemi.
 * Sag tiklayan oyuncu (kontenjan bossa) Avatar olur.
 */
public final class AvatarItem {

    private static final String KEY = "avatar_item";

    private AvatarItem() {
    }

    public static NamespacedKey key(Plugin plugin) {
        return new NamespacedKey(plugin, KEY);
    }

    public static ItemStack create(Plugin plugin) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Msg.item("Avatar Ruhu", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Msg.item("Sağ tık: Avatar ol!", NamedTextColor.YELLOW),
                Msg.item("Aktifken envanterine kilitlenir.", NamedTextColor.GRAY),
                Msg.item("Avatar ölürse ruh el değiştirir!", NamedTextColor.GRAY),
                Msg.item("Sunucuda yalnızca 1 Avatar olabilir.", NamedTextColor.GRAY)));
        Compat.glint(meta);
        meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Ruhu oyuncunun kilitli slotuna yerlestirir; slotta duran esyayi
     * envantere geri verir, yer yoksa yere birakir.
     */
    public static void lockIntoInventory(Plugin plugin, Player player) {
        int soulSlot = plugin.getConfig().getInt("avatar-soul.slot", 35);
        ItemStack occupying = player.getInventory().getItem(soulSlot);
        player.getInventory().setItem(soulSlot, create(plugin));
        if (occupying != null && !occupying.getType().isAir() && !isAvatarItem(plugin, occupying)) {
            for (ItemStack leftover : player.getInventory().addItem(occupying).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    public static boolean isAvatarItem(Plugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key(plugin), PersistentDataType.BYTE);
    }
}
