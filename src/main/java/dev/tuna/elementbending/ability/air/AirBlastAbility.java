package dev.tuna.elementbending.ability.air;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.AbilityUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Hava Patlamasi: yakindaki oyunculari geri iter.
 */
public final class AirBlastAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public AirBlastAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.AIR_BLAST;
    }

    @Override
    public boolean execute(Player player) {
        double radius = plugin.getConfig().getDouble("abilities.air-blast.radius", 5.0);
        double power = plugin.getConfig().getDouble("abilities.air-blast.push-power", 1.5);

        for (org.bukkit.entity.LivingEntity target : AbilityUtil.livingAround(player, radius, true)) {
            Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector());
            push.setY(0);
            if (push.lengthSquared() > 1.0E-4) {
                push.normalize();
            } else {
                push = AbilityUtil.horizontalDirection(player);
            }
            push.multiply(power).setY(0.45);
            target.setVelocity(push);
        }

        player.getWorld().spawnParticle(Compat.PARTICLE_GUST, player.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 40, 1.5, 0.6, 1.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Compat.SOUND_WIND_BURST, 1.0f, 0.9f);
        return true;
    }
}
