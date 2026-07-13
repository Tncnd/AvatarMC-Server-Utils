package dev.tuna.elementbending.command;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.gui.GuideGui;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /guide — rehber menusunu acar.
 */
public final class GuideCommand implements CommandExecutor {

    private final ElementBendingPlugin plugin;

    public GuideCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "Bu komut yalnızca oyuncular içindir.", NamedTextColor.RED);
            return true;
        }
        new GuideGui(plugin).open(player);
        return true;
    }
}
