package dev.tuna.elementbending.command;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.util.DurationParser;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * /mute <oyuncu> <sayi> <dakika|saat|gun> <neden...> — yalnizca chat susturmasi.
 * /unmute <oyuncu> — susturmayi kaldirir.
 */
public final class MuteCommand implements TabExecutor {

    private static final String PERMISSION = "elementbending.mute";

    private final ElementBendingPlugin plugin;

    public MuteCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Msg.send(sender, "Bu komut için yetkin yok!", NamedTextColor.RED);
            return true;
        }
        if (command.getName().equalsIgnoreCase("unmute")) {
            return handleUnmute(sender, args);
        }
        if (args.length < 4) {
            Msg.send(sender, "Kullanım: /mute <oyuncu> <sayı> <dakika|saat|gün> <neden>", NamedTextColor.YELLOW);
            return true;
        }

        Long durationMillis = DurationParser.parseMillis(args[1], args[2]);
        if (durationMillis == null) {
            Msg.send(sender, "Geçersiz süre! Örnek: /mute Oyuncu 30 dakika Spam", NamedTextColor.RED);
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        String durationText = args[1] + " " + DurationParser.unitName(args[2]);

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        plugin.getMuteManager().mute(target.getUniqueId(),
                System.currentTimeMillis() + durationMillis, reason);

        if (target.isOnline() && target instanceof Player online) {
            Msg.send(online, "Susturuldun! Süre: " + durationText + " — Sebep: " + reason, NamedTextColor.RED);
        }
        Bukkit.broadcast(Component.text(Lang.t("🔇 " + target.getName() + ", " + sender.getName() + " tarafından " + durationText + " susturuldu! Sebep: " + reason), NamedTextColor.GOLD));
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Kullanım: /unmute <oyuncu>", NamedTextColor.YELLOW);
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (plugin.getMuteManager().unmute(target.getUniqueId())) {
            Msg.send(sender, target.getName() + " oyuncusunun susturması kaldırıldı.", NamedTextColor.GREEN);
            if (target.isOnline() && target instanceof Player online) {
                Msg.send(online, "Susturman kaldırıldı, tekrar konuşabilirsin.", NamedTextColor.GREEN);
            }
        } else {
            Msg.send(sender, "Bu oyuncu zaten susturulmamış.", NamedTextColor.RED);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!sender.hasPermission(PERMISSION)) {
            return result;
        }
        if (args.length == 1) {
            String current = args[0].toLowerCase(Locale.ROOT);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                    result.add(online.getName());
                }
            }
        } else if (args.length == 3 && command.getName().equalsIgnoreCase("mute")) {
            result.addAll(List.of("dakika", "saat", "gün"));
        }
        return result;
    }
}
