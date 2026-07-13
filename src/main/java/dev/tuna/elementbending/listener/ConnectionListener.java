package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.gui.ElementSelectGui;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Giris olaylari: element secmemis oyuncuya secim menusu
 * OTOMATIK olarak yalnizca 1 kez acilir (players.yml'de takip edilir).
 * Sonraki girislerde yalnizca hatirlatma mesaji gonderilir.
 * Adminler /guiac ile menuyu tekrar acabilir.
 */
public final class ConnectionListener implements Listener {

    private final ElementBendingPlugin plugin;

    public ConnectionListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getElementManager().hasElement(player.getUniqueId())) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || plugin.getElementManager().hasElement(player.getUniqueId())) {
                return;
            }
            if (!plugin.getDataStore().isGuiShown(player.getUniqueId())) {
                plugin.getDataStore().markGuiShown(player.getUniqueId());
                new ElementSelectGui(plugin).open(player);
                Msg.send(player, "Elementini seç! Menüyü kapatırsan tekrar açmak için: /element sec", NamedTextColor.YELLOW);
            } else {
                Msg.send(player, "Henüz bir element seçmedin! Seçim için: /element", NamedTextColor.YELLOW);
            }
        }, 40L);
    }
}
