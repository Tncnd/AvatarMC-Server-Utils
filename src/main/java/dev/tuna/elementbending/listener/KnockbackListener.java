package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Toprak bukucu geri tepme azaltmasi.
 * EntityKnockbackEvent yalnizca yeni Paper surumlerinde bulundugundan
 * bu sinif ana siniftan KOSULLU olarak kaydedilir (Class.forName kontrolu).
 */
public final class KnockbackListener implements Listener {

    private final ElementBendingPlugin plugin;

    public KnockbackListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (plugin.getElementManager().getElement(player.getUniqueId()) != ElementType.EARTH) {
            return;
        }
        double multiplier = plugin.getConfig().getDouble("passives.earth-knockback-multiplier", 0.5);
        event.setKnockback(event.getKnockback().multiply(multiplier));
    }
}
