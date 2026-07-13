package dev.tuna.elementbending.ability.water;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.AbilityUtil;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Sifa Dalgasi: kullaniciyi ve yakindaki ayni elementten
 * takim arkadaslarini iyilestirir, Regen II verir.
 */
public final class HealingWaveAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public HealingWaveAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.HEALING_WAVE;
    }

    @Override
    public boolean execute(Player player) {
        double healAmount = plugin.getConfig().getDouble("abilities.healing-wave.heal-amount", 8.0);
        double radius = plugin.getConfig().getDouble("abilities.healing-wave.radius", 8.0);
        int regenSeconds = plugin.getConfig().getInt("abilities.healing-wave.regen-seconds", 20);

        ElementType casterElement = plugin.getElementManager().getElement(player.getUniqueId());

        applyHeal(player, healAmount, regenSeconds);
        for (Player target : AbilityUtil.playersAround(player, radius)) {
            if (plugin.getElementManager().getElement(target.getUniqueId()) == casterElement) {
                applyHeal(target, healAmount, regenSeconds);
                Msg.actionBar(target, player.getName() + " sana şifa verdi!", NamedTextColor.BLUE);
            }
        }

        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 12, 1.5, 0.8, 1.5, 0);
        player.getWorld().spawnParticle(Compat.PARTICLE_SPLASH, player.getLocation().add(0, 0.5, 0), 60, 2.0, 0.8, 2.0, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.4f);
        return true;
    }

    private void applyHeal(Player target, double amount, int regenSeconds) {
        target.heal(amount);
        target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenSeconds * 20, 1, true, true));
    }
}
