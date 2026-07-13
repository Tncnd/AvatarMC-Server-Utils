package dev.tuna.elementbending.ability.avatar;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Avatar State: sureli guclenme, ardindan kisa zayiflik.
 * Efekt/sure yonetimi AvatarStateManager'dadir.
 */
public final class AvatarStateAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public AvatarStateAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.AVATAR_STATE;
    }

    @Override
    public boolean execute(Player player) {
        plugin.getAvatarStateManager().activate(player);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 60, 0.6, 1.0, 0.6, 0.08);
        return true;
    }
}
