package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.manager.WeaponDisableManager;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Hasar tabanli pasifler: dusme hasari, ates yumrugu,
 * kaya yumrugu bonusu, agir silah engeli ve geri tepme azaltmasi.
 */
public final class DamageListener implements Listener {

    private final ElementBendingPlugin plugin;

    public DamageListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ElementType element = plugin.getElementManager().getElement(player.getUniqueId());
        if (element == ElementType.AIR || element == ElementType.AVATAR) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }
        ElementType element = plugin.getElementManager().getElement(damager.getUniqueId());
        if (element == null) {
            return;
        }

        Material hand = damager.getInventory().getItemInMainHand().getType();

        // Kasirga sonrasi agir silah engeli
        if (WeaponDisableManager.isHeavyWeapon(hand)
                && plugin.getWeaponDisableManager().isDisabled(damager.getUniqueId())) {
            event.setCancelled(true);
            Msg.actionBar(damager, "Silahın rüzgârla savruldu, kısa süre kullanamazsın!", NamedTextColor.AQUA);
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        // Ates bukucu yumrugu hedefi tutusturur
        if (element == ElementType.FIRE && hand.isAir() && event.getEntity() instanceof LivingEntity victim) {
            int seconds = plugin.getConfig().getInt("passives.fire-punch-seconds", 3);
            victim.setFireTicks(Math.max(victim.getFireTicks(), seconds * 20));
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation().add(0, 1, 0), 8, 0.3, 0.4, 0.3, 0.02);
        }

        // Kaya Yumrugu bonusu (Kuvvet efektiyle dogal olarak birlesir)
        if (element == ElementType.EARTH && plugin.getEmpowerManager().consume(damager.getUniqueId())) {
            double bonus = plugin.getConfig().getDouble("abilities.rock-punch.bonus-damage", 4.0);
            event.setDamage(event.getDamage() + bonus);
            event.getEntity().getWorld().spawnParticle(Compat.PARTICLE_BLOCK,
                    event.getEntity().getLocation().add(0, 1, 0),
                    20, 0.3, 0.4, 0.3, Material.STONE.createBlockData());
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(),
                    Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);
        }
    }

}
