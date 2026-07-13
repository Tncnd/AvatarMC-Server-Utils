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
 * Hava Sicrayisi: oyuncuyu havaya firlatir.
 */
public final class AirJumpAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public AirJumpAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.AIR_JUMP;
    }

    @Override
    public boolean execute(Player player) {
        double power = plugin.getConfig().getDouble("abilities.air-jump.launch-power", 1.15);
        Vector direction = AbilityUtil.horizontalDirection(player).multiply(0.35);
        player.setVelocity(new Vector(direction.getX(), power, direction.getZ()));

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 0.4, 0.2, 0.4, 0.05);
        player.getWorld().playSound(player.getLocation(), Compat.SOUND_WIND_BURST, 1.0f, 1.3f);
        return true;
    }
}
