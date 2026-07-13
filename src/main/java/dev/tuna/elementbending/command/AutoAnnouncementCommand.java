package dev.tuna.elementbending.command;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.manager.AnnouncementManager;
import dev.tuna.elementbending.util.DurationParser;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * /autoannouncement <mesaj> <sayi> <dakika|saat> — imzasiz tekrarlayan duyuru.
 * /autoannouncement liste — mevcut otomatik duyurular
 * /autoannouncement sil <id> — otomatik duyuruyu kaldirir
 */
public final class AutoAnnouncementCommand implements TabExecutor {

    private static final String PERMISSION = "elementbending.announce";

    private final ElementBendingPlugin plugin;

    public AutoAnnouncementCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Msg.send(sender, "Bu komut için yetkin yok!", NamedTextColor.RED);
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("liste") || first.equals("list")) {
            var autos = plugin.getAnnouncementManager().view();
            if (autos.isEmpty()) {
                Msg.send(sender, "Kayıtlı otomatik duyuru yok.", NamedTextColor.YELLOW);
                return true;
            }
            for (AnnouncementManager.AutoAnnouncement entry : autos.values()) {
                Msg.send(sender, "#" + entry.id() + " [" + DurationParser.formatRemaining(entry.intervalMillis())
                        + " arayla] " + entry.message(), NamedTextColor.GRAY);
            }
            return true;
        }
        if (first.equals("sil") || first.equals("remove")) {
            if (args.length < 2) {
                Msg.send(sender, "Kullanım: /autoannouncement sil <id>", NamedTextColor.YELLOW);
                return true;
            }
            try {
                int id = Integer.parseInt(args[1]);
                if (plugin.getAnnouncementManager().remove(id)) {
                    Msg.send(sender, "#" + id + " numaralı otomatik duyuru silindi.", NamedTextColor.GREEN);
                } else {
                    Msg.send(sender, "Bu id ile bir duyuru bulunamadı. Liste: /autoannouncement liste", NamedTextColor.RED);
                }
            } catch (NumberFormatException ex) {
                Msg.send(sender, "Geçersiz id!", NamedTextColor.RED);
            }
            return true;
        }

        // Olusturma: son iki arguman sayi + birim, geri kalani mesaj
        if (args.length < 3) {
            sendUsage(sender);
            return true;
        }
        String amountArg = args[args.length - 2];
        String unitArg = args[args.length - 1];
        Long intervalMillis = DurationParser.parseMillis(amountArg, unitArg);
        if (intervalMillis == null || intervalMillis < 60_000L) {
            Msg.send(sender, "Geçersiz süre! En az 1 dakika olmalı. Örnek: /autoannouncement Discord'a katılın! 2 saat", NamedTextColor.RED);
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 2));
        AnnouncementManager.AutoAnnouncement entry = plugin.getAnnouncementManager().add(message, intervalMillis);
        Msg.send(sender, "#" + entry.id() + " numaralı otomatik duyuru eklendi ("
                + amountArg + " " + DurationParser.unitName(unitArg) + " arayla). Silmek için: /autoannouncement sil " + entry.id(), NamedTextColor.GREEN);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        Msg.send(sender, "Kullanım: /autoannouncement <mesaj> <sayı> <dakika|saat>", NamedTextColor.YELLOW);
        Msg.send(sender, "Diğer: /autoannouncement liste | /autoannouncement sil <id>", NamedTextColor.GRAY);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!sender.hasPermission(PERMISSION)) {
            return result;
        }
        if (args.length == 1) {
            String current = args[0].toLowerCase(Locale.ROOT);
            for (String option : List.of("liste", "sil")) {
                if (option.startsWith(current)) {
                    result.add(option);
                }
            }
        }
        return result;
    }
}
