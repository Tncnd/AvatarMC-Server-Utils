package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * "Shift+1..5" tetikleyicisi: sunucu klavye kombinasyonlarini goremedigi icin
 * egilme (Shift) + hotbar slot degisimi (1-5 tuslari) kullanilir.
 */
public final class HotbarListener implements Listener {

    private final ElementBendingPlugin plugin;

    public HotbarListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        ElementType element = plugin.getElementManager().getElement(player.getUniqueId());
        if (element == null) {
            return;
        }
        AbilityKey key = plugin.getKeybindManager().getBind(player.getUniqueId(), element, event.getNewSlot());
        if (key == null) {
            return;
        }
        event.setCancelled(true);
        plugin.getAbilityManager().tryUse(player, key);
    }
}
