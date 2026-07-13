package dev.tuna.elementbending.command;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.gui.ElementSelectGui;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /guiac <oyuncu> — bir oyuncunun onune element secim menusunu acar (admin).
 */
public final class GuiAcCommand implements TabExecutor {

    private static final String ADMIN_PERMISSION = "elementbending.admin";

    private final ElementBendingPlugin plugin;

    public GuiAcCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            Msg.send(sender, "Bu komut için yetkin yok!", NamedTextColor.RED);
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, "Kullanım: /guiac <oyuncu>", NamedTextColor.YELLOW);
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Msg.send(sender, "Oyuncu bulunamadı (çevrimiçi olmalı).", NamedTextColor.RED);
            return true;
        }
        if (plugin.getElementManager().hasElement(target.getUniqueId())) {
            Msg.send(sender, "Uyarı: " + target.getName() + " zaten element seçmiş; menü açıldı ama seçim yapamaz. Önce /element admin reset " + target.getName(), NamedTextColor.YELLOW);
        }
        new ElementSelectGui(plugin).open(target);
        Msg.send(target, "Element seçim menüsü açıldı!", NamedTextColor.YELLOW);
        Msg.send(sender, target.getName() + " oyuncusuna seçim menüsü açıldı.", NamedTextColor.GREEN);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission(ADMIN_PERMISSION)) {
            String current = args[0].toLowerCase(Locale.ROOT);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                    result.add(online.getName());
                }
            }
        }
        return result;
    }
}
