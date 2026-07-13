package dev.tuna.elementbending.task;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.util.AvatarCompass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Cevrimici oyuncularin envanterindeki Avatar Pusulalarini gunceller:
 * yerde ruh varsa lodestone hedefi olarak ruhun konumunu isaretler,
 * yoksa hedefi kaldirir (pusula ignesi bosta doner).
 */
public final class CompassTask extends BukkitRunnable {

    private final ElementBendingPlugin plugin;

    public CompassTask(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Location soulLocation = plugin.getSoulItemManager().getDroppedSoulLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack[] contents = player.getInventory().getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack item = contents[slot];
                if (AvatarCompass.isAvatarCompass(plugin, item) && updateCompass(item, soulLocation)) {
                    player.getInventory().setItem(slot, item);
                }
            }
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (AvatarCompass.isAvatarCompass(plugin, offHand) && updateCompass(offHand, soulLocation)) {
                player.getInventory().setItemInOffHand(offHand);
            }
        }
    }

    /**
     * @return meta degistiyse true (gereksiz paket gonderimini onlemek icin)
     */
    private boolean updateCompass(ItemStack item, Location target) {
        if (!(item.getItemMeta() instanceof CompassMeta meta)) {
            return false;
        }
        Location current = meta.hasLodestone() ? meta.getLodestone() : null;
        if (sameBlock(current, target)) {
            return false;
        }
        meta.setLodestoneTracked(false);
        meta.setLodestone(target);
        item.setItemMeta(meta);
        return true;
    }

    private boolean sameBlock(Location a, Location b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getWorld() != null && b.getWorld() != null
                && a.getWorld().equals(b.getWorld())
                && a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ();
    }
}
