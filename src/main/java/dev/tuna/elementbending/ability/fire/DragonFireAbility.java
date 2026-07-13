package dev.tuna.elementbending.ability.fire;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.AbilityUtil;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Ejderha Alevi: 15 sn boyunca Fire Charge yerine Ghast Fireball
 * atilmasini saglar ve etraftakileri atese verir.
 */
public final class DragonFireAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public DragonFireAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.DRAGON_FIRE;
    }

    @Override
    public boolean execute(Player player) {
        int duration = plugin.getConfig().getInt("abilities.dragon-fire.duration-seconds", 15);
        double radius = plugin.getConfig().getDouble("abilities.dragon-fire.ignite-radius", 6.0);

        plugin.getFireUltManager().activate(player.getUniqueId(), duration);
        for (org.bukkit.entity.LivingEntity target : AbilityUtil.livingAround(player, radius, true)) {
            target.setFireTicks(Math.max(target.getFireTicks(), 80));
        }

        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 80, 2.0, 1.0, 2.0, 0.05);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 15, 1.0, 0.5, 1.0, 0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.0f);
        Msg.send(player, "Ejderha Alevi aktif! " + duration + " sn boyunca boş elle sağ tık = Ghast Fireball.", NamedTextColor.RED);
        return true;
    }
}
