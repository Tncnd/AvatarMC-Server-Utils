package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.util.Compat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kasirga sonrasi mace/spear kullanimini gecici olarak engeller.
 */
public final class WeaponDisableManager {

    /** Spear ve MACE her surumde bulunmadigi icin calisma zamaninda cozulur. */
    private static final Material SPEAR = Material.matchMaterial("SPEAR");
    private static final Material MACE = Compat.MACE;

    private final Map<UUID, Long> disabled = new ConcurrentHashMap<>();

    public void disable(Player player, int seconds) {
        disabled.put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
        int ticks = seconds * 20;
        if (MACE != null) {
            player.setCooldown(MACE, ticks);
        }
        if (SPEAR != null) {
            player.setCooldown(SPEAR, ticks);
        }
    }

    public boolean isDisabled(UUID uuid) {
        Long end = disabled.get(uuid);
        if (end == null) {
            return false;
        }
        if (end <= System.currentTimeMillis()) {
            disabled.remove(uuid);
            return false;
        }
        return true;
    }

    public static boolean isHeavyWeapon(Material material) {
        return (MACE != null && material == MACE) || (SPEAR != null && material == SPEAR);
    }
}
