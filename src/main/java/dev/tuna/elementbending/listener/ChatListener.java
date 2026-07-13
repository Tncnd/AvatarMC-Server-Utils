package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.manager.MuteManager;
import dev.tuna.elementbending.util.DurationParser;
import dev.tuna.elementbending.util.Msg;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.Set;

/**
 * Susturulmus oyuncularin chat mesajlarini engeller.
 */
public final class ChatListener implements Listener {

    private final ElementBendingPlugin plugin;

    public ChatListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    /** Susturmanin komutlarla asilmasini engelle (/me, /say, /tell...). */
    private static final Set<String> CHAT_COMMANDS = Set.of(
            "me", "say", "tell", "msg", "w", "whisper", "teammsg", "tm");

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        MuteManager.MuteEntry mute = plugin.getMuteManager().getActiveMute(event.getPlayer().getUniqueId());
        if (mute == null) {
            return;
        }
        String root = event.getMessage().substring(1).split(" ", 2)[0].toLowerCase(Locale.ROOT);
        if (root.startsWith("minecraft:")) {
            root = root.substring("minecraft:".length());
        }
        if (CHAT_COMMANDS.contains(root)) {
            event.setCancelled(true);
            long remaining = mute.endMillis() - System.currentTimeMillis();
            Msg.send(event.getPlayer(), "Susturuldun! Kalan: " + DurationParser.formatRemaining(remaining)
                    + " — Sebep: " + mute.reason(), NamedTextColor.RED);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        MuteManager.MuteEntry mute = plugin.getMuteManager().getActiveMute(event.getPlayer().getUniqueId());
        if (mute == null) {
            return;
        }
        event.setCancelled(true);
        long remaining = mute.endMillis() - System.currentTimeMillis();
        Msg.send(event.getPlayer(), "Susturuldun! Kalan: " + DurationParser.formatRemaining(remaining)
                + " — Sebep: " + mute.reason(), NamedTextColor.RED);
    }
}
