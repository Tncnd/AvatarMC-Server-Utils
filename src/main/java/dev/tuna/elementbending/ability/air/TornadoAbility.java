package dev.tuna.elementbending.ability.air;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.AbilityUtil;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Kasirga: yakindaki oyunculari havaya savurur ve agir silahlarini
 * (mace/spear) kisa sure kullanilamaz yapar.
 */
public final class TornadoAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public TornadoAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.TORNADO;
    }

    @Override
    public boolean execute(Player player) {
        double radius = plugin.getConfig().getDouble("abilities.tornado.radius", 8.0);
        double power = plugin.getConfig().getDouble("abilities.tornado.launch-power", 2.2);
        int disableSeconds = plugin.getConfig().getInt("abilities.tornado.weapon-disable-seconds", 5);

        List<LivingEntity> targets = AbilityUtil.livingAround(player, radius, true);
        for (LivingEntity target : targets) {
            target.setVelocity(new Vector(0, power, 0));
            if (target instanceof Player targetPlayer) {
                plugin.getWeaponDisableManager().disable(targetPlayer, disableSeconds);
                Msg.actionBar(targetPlayer, "Kasırgaya kapıldın! Ağır silahların " + disableSeconds + " sn devre dışı.", NamedTextColor.AQUA);
            }
        }

        spawnSpiral(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Compat.SOUND_WIND_BURST, 1.5f, 0.6f);
        return true;
    }

    private void spawnSpiral(Location center) {
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick++ >= 20 || center.getWorld() == null) {
                    cancel();
                    return;
                }
                double height = tick * 0.3;
                for (int i = 0; i < 3; i++) {
                    double angle = (tick * 0.6) + (i * Math.PI * 2 / 3);
                    double x = Math.cos(angle) * 2.0;
                    double z = Math.sin(angle) * 2.0;
                    center.getWorld().spawnParticle(Particle.CLOUD,
                            center.clone().add(x, height, z), 2, 0.1, 0.1, 0.1, 0.02);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
