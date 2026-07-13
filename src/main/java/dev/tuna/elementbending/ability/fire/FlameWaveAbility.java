package dev.tuna.elementbending.ability.fire;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.AbilityUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Alev Dalgasi: onundeki oyunculari atese verir.
 */
public final class FlameWaveAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public FlameWaveAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.FLAME_WAVE;
    }

    @Override
    public boolean execute(Player player) {
        double range = plugin.getConfig().getDouble("abilities.flame-wave.range", 6.0);
        int fireSeconds = plugin.getConfig().getInt("abilities.flame-wave.fire-seconds", 5);

        for (org.bukkit.entity.LivingEntity target : AbilityUtil.livingInCone(player, range, 0.45)) {
            target.setFireTicks(Math.max(target.getFireTicks(), fireSeconds * 20));
        }

        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location origin = player.getEyeLocation();
        for (double distance = 1.0; distance <= range; distance += 0.5) {
            Location point = origin.clone().add(direction.clone().multiply(distance));
            double spread = distance * 0.25;
            player.getWorld().spawnParticle(Particle.FLAME, point, 6, spread, spread, spread, 0.01);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
        return true;
    }
}
