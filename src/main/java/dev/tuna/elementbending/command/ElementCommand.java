package dev.tuna.elementbending.command;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.gui.ElementSelectGui;
import dev.tuna.elementbending.gui.KeybindGui;
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
 * /element komutu: sec, tus, bilgi ve admin alt komutlari.
 */
public final class ElementCommand implements TabExecutor {

    private static final String ADMIN_PERMISSION = "elementbending.admin";

    private final ElementBendingPlugin plugin;

    public ElementCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            return handleAdmin(sender, args);
        }

        if (!(sender instanceof Player player)) {
            Msg.send(sender, "Bu komut yalnızca oyuncular içindir. Admin: /element admin", NamedTextColor.RED);
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (sub) {
            case "", "bilgi", "info" -> {
                if (!plugin.getElementManager().hasElement(player.getUniqueId()) && sub.isEmpty()) {
                    new ElementSelectGui(plugin).open(player);
                } else {
                    sendInfo(player);
                }
            }
            case "sec", "secim", "select" -> {
                if (plugin.getElementManager().hasElement(player.getUniqueId())) {
                    Msg.send(player, "Zaten bir element seçtin. Seçimler kalıcıdır!", NamedTextColor.RED);
                } else {
                    new ElementSelectGui(plugin).open(player);
                }
            }
            case "tus", "keybind", "bind" -> {
                ElementType element = plugin.getElementManager().getElement(player.getUniqueId());
                if (element == null) {
                    Msg.send(player, "Önce bir element seçmelisin: /element sec", NamedTextColor.RED);
                } else {
                    new KeybindGui(plugin, player, element).open(player);
                }
            }
            default -> Msg.send(player, "Kullanım: /element [sec | tus | bilgi]", NamedTextColor.YELLOW);
        }
        return true;
    }

    private void sendInfo(Player player) {
        ElementType element = plugin.getElementManager().getElement(player.getUniqueId());
        if (element == null) {
            Msg.send(player, "Henüz bir element seçmedin: /element sec", NamedTextColor.YELLOW);
            return;
        }
        Msg.send(player, "Elementin: " + element.getDisplayName(), element.getColor());
        List<AbilityKey> binds = plugin.getKeybindManager().getBinds(player.getUniqueId(), element);
        for (int i = 0; i < binds.size(); i++) {
            AbilityKey key = binds.get(i);
            int remaining = plugin.getCooldownManager().remainingSeconds(player.getUniqueId(), key.name());
            String cooldownInfo = remaining > 0 ? " (⏳ " + remaining + " sn)" : "";
            Msg.send(player, "Shift+" + (i + 1) + " → " + key.getDisplayName() + cooldownInfo, NamedTextColor.GRAY);
        }
        Msg.send(player, "Tuş düzeni için: /element tus", NamedTextColor.DARK_GRAY);
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            Msg.send(sender, "Bu komut için yetkin yok!", NamedTextColor.RED);
            return true;
        }
        if (args.length < 2) {
            Msg.send(sender, "Kullanım: /element admin <set|reset|list|maxplayerperelement> ...", NamedTextColor.YELLOW);
            return true;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "set" -> {
                if (args.length < 4) {
                    Msg.send(sender, "Kullanım: /element admin set <oyuncu> <element>", NamedTextColor.YELLOW);
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    Msg.send(sender, "Oyuncu bulunamadı (çevrimiçi olmalı).", NamedTextColor.RED);
                    return true;
                }
                ElementType type = ElementType.fromName(args[3]);
                if (type == null) {
                    Msg.send(sender, "Geçersiz element! (AIR, WATER, FIRE, EARTH, AVATAR)", NamedTextColor.RED);
                    return true;
                }
                plugin.getElementManager().setElementForce(target.getUniqueId(), type);
                Msg.send(sender, target.getName() + " → " + type.getDisplayName() + " olarak ayarlandı.", NamedTextColor.GREEN);
                Msg.send(target, "Elementin yönetici tarafından " + type.getDisplayName() + " olarak ayarlandı!", type.getColor());
            }
            case "reset" -> {
                if (args.length < 3) {
                    Msg.send(sender, "Kullanım: /element admin reset <oyuncu>", NamedTextColor.YELLOW);
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    Msg.send(sender, "Oyuncu bulunamadı (çevrimiçi olmalı).", NamedTextColor.RED);
                    return true;
                }
                plugin.getElementManager().setElementForce(target.getUniqueId(), null);
                plugin.getCooldownManager().clear(target.getUniqueId());
                Msg.send(sender, target.getName() + " oyuncusunun elementi sıfırlandı.", NamedTextColor.GREEN);
                Msg.send(target, "Elementin sıfırlandı. Yeni seçim: /element sec", NamedTextColor.YELLOW);
            }
            case "maxplayerperelement" -> {
                if (args.length < 3) {
                    Msg.send(sender, "Kullanım: /element admin maxplayerperelement <sayı>", NamedTextColor.YELLOW);
                    return true;
                }
                int newLimit;
                try {
                    newLimit = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    Msg.send(sender, "Geçersiz sayı!", NamedTextColor.RED);
                    return true;
                }
                if (newLimit < 1 || newLimit > 10000) {
                    Msg.send(sender, "Sayı 1 ile 10000 arasında olmalı!", NamedTextColor.RED);
                    return true;
                }
                plugin.getConfig().set("limits.per-element", newLimit);
                plugin.saveConfig();
                Msg.send(sender, "Element başına maksimum oyuncu sayısı " + newLimit + " olarak ayarlandı.", NamedTextColor.GREEN);
                Msg.send(sender, "Not: limit düşürülürse mevcut seçimler silinmez, yalnızca yeni seçimler engellenir.", NamedTextColor.GRAY);
            }
            case "removesoul" -> {
                int removed = plugin.getSoulItemManager().removeAllDropped();
                if (removed > 0) {
                    Msg.send(sender, removed + " adet yerdeki Avatar Ruhu kaldırıldı.", NamedTextColor.GREEN);
                } else {
                    Msg.send(sender, "Yerde takip edilen Avatar Ruhu yok.", NamedTextColor.YELLOW);
                }
            }
            case "list" -> {
                for (ElementType type : ElementType.values()) {
                    Msg.send(sender, type.getDisplayName() + ": "
                            + plugin.getElementManager().count(type) + "/"
                            + plugin.getElementManager().maxFor(type), type.getColor());
                }
            }
            default -> Msg.send(sender, "Kullanım: /element admin <set|reset|list|maxplayerperelement>", NamedTextColor.YELLOW);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.add("sec");
            result.add("tus");
            result.add("bilgi");
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                result.add("admin");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin") && sender.hasPermission(ADMIN_PERMISSION)) {
            result.add("set");
            result.add("reset");
            result.add("list");
            result.add("maxplayerperelement");
            result.add("removesoul");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin")
                && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("reset"))) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                result.add(online.getName());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("set")) {
            for (ElementType type : ElementType.values()) {
                result.add(type.name());
            }
        }
        String current = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ROOT) : "";
        result.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(current));
        return result;
    }
}
