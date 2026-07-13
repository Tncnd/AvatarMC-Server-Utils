package dev.tuna.elementbending.ability.earth;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.AbilityUtil;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Deprem: yakindaki oyunculara Slowness III uygular ve onlari sarsar.
 */
public final class EarthquakeAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public EarthquakeAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.EARTHQUAKE;
    }

    @Override
    public boolean execute(Player player) {
        double radius = plugin.getConfig().getDouble("abilities.earthquake.radius", 8.0);
        int slownessSeconds = plugin.getConfig().getInt("abilities.earthquake.slowness-seconds", 6);

        List<LivingEntity> targets = AbilityUtil.livingAround(player, radius, false);
        for (LivingEntity target : targets) {
            target.addPotionEffect(new PotionEffect(Compat.SLOWNESS, slownessSeconds * 20, 2, true, true));
            if (target instanceof Player targetPlayer) {
                Msg.actionBar(targetPlayer, "Yer sarsılıyor!", NamedTextColor.DARK_GREEN);
            }
        }

        // Sarsinti: kisa araliklarla kucuk dikey itmeler + blok parcaciklari
        new BukkitRunnable() {
            private int iteration = 0;

            @Override
            public void run() {
                if (iteration++ >= 8) {
                    cancel();
                    return;
                }
                for (LivingEntity target : targets) {
                    if (!target.isValid()) {
                        continue;
                    }
                    target.setVelocity(target.getVelocity().setY(0.12));
                    target.getWorld().spawnParticle(Compat.PARTICLE_BLOCK, target.getLocation(),
                            15, 0.4, 0.1, 0.4, Material.DIRT.createBlockData());
                }
            }
        }.runTaskTimer(plugin, 0L, 3L);

        player.getWorld().spawnParticle(Compat.PARTICLE_BLOCK, player.getLocation(),
                120, radius / 2, 0.3, radius / 2, Material.STONE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.5f);
        return true;
    }
}
