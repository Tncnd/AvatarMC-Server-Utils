package dev.tuna.elementbending.command;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /announcement <mesaj> — yazan kisinin adiyla birlikte vurgulu duyuru yayinlar.
 */
public final class AnnouncementCommand implements CommandExecutor {

    private static final String PERMISSION = "elementbending.announce";

    private final ElementBendingPlugin plugin;

    public AnnouncementCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Msg.send(sender, "Bu komut için yetkin yok!", NamedTextColor.RED);
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, "Kullanım: /announcement <mesaj>", NamedTextColor.YELLOW);
            return true;
        }
        plugin.getAnnouncementManager().broadcast(String.join(" ", args), sender.getName());
        return true;
    }
}
