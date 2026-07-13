package dev.tuna.elementbending.ability.water;

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
 * Su Itisi: onundeki oyunculari su kuvvetiyle geri iter.
 */
public final class WaterPushAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public WaterPushAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.WATER_PUSH;
    }

    @Override
    public boolean execute(Player player) {
        double range = plugin.getConfig().getDouble("abilities.water-push.range", 6.0);
        double power = plugin.getConfig().getDouble("abilities.water-push.push-power", 1.4);

        for (org.bukkit.entity.LivingEntity target : AbilityUtil.livingInCone(player, range, 0.4)) {
            Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector());
            push.setY(0);
            if (push.lengthSquared() > 1.0E-4) {
                push.normalize();
            } else {
                push = AbilityUtil.horizontalDirection(player);
            }
            push.multiply(power).setY(0.3);
            target.setVelocity(push);
        }

        Vector direction = player.getEyeLocation().getDirection().normalize();
        for (double distance = 1.0; distance <= range; distance += 0.5) {
            player.getWorld().spawnParticle(Compat.PARTICLE_SPLASH,
                    player.getEyeLocation().clone().add(direction.clone().multiply(distance)),
                    12, 0.4, 0.4, 0.4, 0.1);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.9f);
        return true;
    }
}
