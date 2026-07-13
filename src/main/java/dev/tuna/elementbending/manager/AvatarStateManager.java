package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Avatar State surecini yonetir: efektler, sure ve bitis zayifligi.
 */
public final class AvatarStateManager {

    private final ElementBendingPlugin plugin;
    private final Map<UUID, Long> active = new ConcurrentHashMap<>();

    public AvatarStateManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    public void activate(Player player) {
        UUID uuid = player.getUniqueId();
        int duration = plugin.getConfig().getInt("avatar-state.duration-seconds", 15);
        active.put(uuid, System.currentTimeMillis() + duration * 1000L);

        int ticks = duration * 20;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 1, true, true));
        player.addPotionEffect(new PotionEffect(Compat.RESISTANCE, ticks, 0, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, ticks, 0, true, true));

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        Msg.send(player, "Avatar State aktif! (" + duration + " sn)", NamedTextColor.LIGHT_PURPLE);

        Bukkit.getScheduler().runTaskLater(plugin, () -> end(uuid), ticks);
    }

    private void end(UUID uuid) {
        if (active.remove(uuid) == null) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }
        int weakness = plugin.getConfig().getInt("avatar-state.weakness-seconds", 15);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weakness * 20, 0, true, true));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
        Msg.send(player, "Avatar State sona erdi. " + weakness + " sn boyunca zayıfsın.", NamedTextColor.GRAY);
    }

    public boolean isActive(UUID uuid) {
        Long end = active.get(uuid);
        if (end == null) {
            return false;
        }
        if (end <= System.currentTimeMillis()) {
            active.remove(uuid);
            return false;
        }
        return true;
    }

    public double cooldownMultiplier() {
        return plugin.getConfig().getDouble("avatar-state.cooldown-multiplier", 0.75);
    }
}
