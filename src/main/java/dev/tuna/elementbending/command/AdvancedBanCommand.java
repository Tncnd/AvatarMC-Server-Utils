package dev.tuna.elementbending.command;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.util.DurationParser;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * /advancedban <oyuncu> <sayi> <dakika|saat|gun> <neden...>
 * Sureli ban atar; oyuncu cevrimiciyse aninda atilir.
 * Kaldirmak icin vanilla /pardon kullanilabilir.
 */
public final class AdvancedBanCommand implements TabExecutor {

    private static final String PERMISSION = "elementbending.ban";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Msg.send(sender, "Bu komut için yetkin yok!", NamedTextColor.RED);
            return true;
        }
        if (args.length < 4) {
            Msg.send(sender, "Kullanım: /advancedban <oyuncu> <sayı> <dakika|saat|gün> <neden>", NamedTextColor.YELLOW);
            return true;
        }

        Long durationMillis = DurationParser.parseMillis(args[1], args[2]);
        if (durationMillis == null) {
            Msg.send(sender, "Geçersiz süre! Örnek: /advancedban Oyuncu 3 gün Hacking", NamedTextColor.RED);
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        String durationText = args[1] + " " + DurationParser.unitName(args[2]);
        Date expires = new Date(System.currentTimeMillis() + durationMillis);

        Player onlineTarget = Bukkit.getPlayerExact(args[0]);
        if (onlineTarget != null && onlineTarget.hasPermission(PERMISSION)) {
            Msg.send(sender, "Bu oyuncunun da ban yetkisi var; onu banlayamazsın!", NamedTextColor.RED);
            return true;
        }
        OfflinePlayer target = onlineTarget != null ? onlineTarget : Bukkit.getOfflinePlayer(args[0]);
        if (!target.isOnline() && !target.hasPlayedBefore()) {
            Msg.send(sender, "Uyarı: bu isimde bir oyuncu sunucuya hiç girmemiş; isim yanlışsa ban isabet etmez.", NamedTextColor.YELLOW);
        }
        ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
        banList.addBan(target.getPlayerProfile(), reason, expires, sender.getName());

        if (target.isOnline() && target instanceof Player online) {
            online.kick(Component.text(Lang.t("Sunucudan yasaklandın!\n\n"), NamedTextColor.RED)
                    .append(Component.text(Lang.t("Süre: " + durationText + "\n"), NamedTextColor.YELLOW))
                    .append(Component.text(Lang.t("Sebep: " + reason), NamedTextColor.GRAY)));
        }

        Bukkit.broadcast(Component.text(Lang.t("⛔ " + target.getName() + ", " + sender.getName() + " tarafından " + durationText + " yasaklandı! Sebep: " + reason), NamedTextColor.RED));
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
        } else if (args.length == 3) {
            result.addAll(List.of("dakika", "saat", "gün"));
        }
        return result;
    }
}
