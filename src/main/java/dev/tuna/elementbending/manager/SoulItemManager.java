package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.util.AvatarItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Yere dusen Avatar Ruhu'nu korur:
 * - Despawn olmaz (unlimited lifetime + ItemDespawnEvent iptali)
 * - Hasardan etkilenmez (invulnerable)
 * - Moblar alamaz
 * - Lag temizleyici bir plugin item'i silerse bekci gorevi ayni yere yeniden dogurur
 * - Void'e dusecekse o kolondaki en yuksek blogun ustune birakilir
 */
public final class SoulItemManager {

    private static final long GUARD_PERIOD_TICKS = 100L;

    private final ElementBendingPlugin plugin;
    /** Korunan ruh item entity'leri: entity UUID -> son bilinen konum. */
    private final Map<UUID, Location> tracked = new HashMap<>();

    public SoulItemManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    /** Bekci gorevini baslatir (onEnable'da cagrilir). */
    public void startGuardTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::guard, GUARD_PERIOD_TICKS, GUARD_PERIOD_TICKS);
    }

    /**
     * Belirtilen konumda korumali bir Avatar Ruhu dusurur.
     * Konum void'deyse kolondaki en yuksek blogun ustune tasinir.
     */
    public Item spawnProtected(Location location) {
        Location safe = safeLocation(location);
        Item item = safe.getWorld().dropItem(safe, AvatarItem.create(plugin));
        configure(item);
        tracked.put(item.getUniqueId(), item.getLocation().clone());
        plugin.getServer().broadcast(Component.text(
                "☠ AVATAR RUHU yere düştü! İlk alan sahibi olur! Avatar Pusulası ruhun yerini gösterir.",
                NamedTextColor.LIGHT_PURPLE));
        return item;
    }

    /** Chunk yuklenince bulunan eski ruh itemlerini yeniden takibe alir. */
    public void track(Item item) {
        configure(item);
        tracked.put(item.getUniqueId(), item.getLocation().clone());
    }

    public void untrack(UUID entityId) {
        tracked.remove(entityId);
    }

    public boolean isTracked(UUID entityId) {
        return tracked.containsKey(entityId);
    }

    /**
     * Yerdeki tum korumali ruhlari kaldirir (admin temizligi).
     * Once takipten cikarilir ki bekci gorevi yeniden dogurtmasin.
     */
    public int removeAllDropped() {
        int removed = 0;
        for (UUID entityId : Map.copyOf(tracked).keySet()) {
            tracked.remove(entityId);
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();
                removed++;
            }
        }
        return removed;
    }

    /** Su an yerde duran ruhun konumu; yerde ruh yoksa null. */
    public Location getDroppedSoulLocation() {
        for (Location location : tracked.values()) {
            return location.clone();
        }
        return null;
    }

    private void configure(Item item) {
        item.setUnlimitedLifetime(true);
        item.setPersistent(true);
        item.setInvulnerable(true);
        item.setCanMobPickup(false);
        item.setVelocity(new Vector(0, 0.1, 0));
        item.customName(Component.text("Avatar Ruhu", NamedTextColor.LIGHT_PURPLE));
        item.setCustomNameVisible(true);
        item.setGlowing(true);
    }

    /**
     * Bekci: takip edilen ruh, chunk yuklu oldugu halde ortadan kalktiysa
     * (ornegin lag temizleyici sildiyse) ayni konumda yeniden dogurulur.
     */
    private void guard() {
        for (Map.Entry<UUID, Location> entry : Map.copyOf(tracked).entrySet()) {
            Location location = entry.getValue();
            if (location.getWorld() == null || !location.isChunkLoaded()) {
                continue;
            }
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity instanceof Item item && item.isValid()) {
                // Konumu guncelle (item akmis/itilmis olabilir)
                tracked.put(entry.getKey(), item.getLocation().clone());
                continue;
            }
            // Item silinmis: yeniden dogur
            tracked.remove(entry.getKey());
            Item respawned = location.getWorld().dropItem(safeLocation(location), AvatarItem.create(plugin));
            configure(respawned);
            tracked.put(respawned.getUniqueId(), respawned.getLocation().clone());
            plugin.getLogger().warning("Avatar Ruhu bir eklenti tarafindan silinmisti; yeniden dogruldu: "
                    + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        }
    }

    private Location safeLocation(Location location) {
        World world = location.getWorld();
        Location safe = location.clone();
        if (safe.getY() < world.getMinHeight() + 1) {
            int highestY = world.getHighestBlockYAt(safe.getBlockX(), safe.getBlockZ());
            safe.setY(Math.max(highestY + 1, world.getMinHeight() + 1));
        }
        return safe;
    }
}
