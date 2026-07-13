package dev.tuna.elementbending.ability.earth;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Kaya Yumrugu: sonraki yakin dovus vurusuna ekstra hasar ekler.
 * Kuvvet (Strength) efektiyle dogal olarak birlesir.
 */
public final class RockPunchAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public RockPunchAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.ROCK_PUNCH;
    }

    @Override
    public boolean execute(Player player) {
        int armSeconds = plugin.getConfig().getInt("abilities.rock-punch.arm-seconds", 10);
        double bonus = plugin.getConfig().getDouble("abilities.rock-punch.bonus-damage", 4.0);

        plugin.getEmpowerManager().arm(player.getUniqueId(), armSeconds);

        player.getWorld().spawnParticle(Compat.PARTICLE_BLOCK, player.getLocation().add(0, 1, 0),
                25, 0.4, 0.4, 0.4, Material.STONE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
        Msg.actionBar(player, "Sonraki yumruğun +" + (int) bonus + " hasar verecek! (" + armSeconds + " sn)", NamedTextColor.DARK_GREEN);
        return true;
    }
}
