package dev.tuna.elementbending.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Mesaj yardimcilari.
 */
public final class Msg {

    private static final Component PREFIX = Component.text("⬢ Element » ", NamedTextColor.GOLD);

    private Msg() {
    }

    public static void send(CommandSender sender, String message, NamedTextColor color) {
        sender.sendMessage(PREFIX.append(Component.text(Lang.t(message), color)));
    }

    public static void actionBar(Player player, String message, NamedTextColor color) {
        player.sendActionBar(Component.text(Lang.t(message), color));
    }

    /**
     * GUI itemleri icin italik olmayan metin.
     */
    public static Component item(String text, NamedTextColor color) {
        return Component.text(Lang.t(text), color).decoration(TextDecoration.ITALIC, false);
    }
}
