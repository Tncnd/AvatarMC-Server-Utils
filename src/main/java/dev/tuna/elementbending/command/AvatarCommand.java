package dev.tuna.elementbending.command;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.AvatarItem;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * /avatar transfer <oyuncu> — devir teklifi gonderir.
 * /avatar kabul — teklifi 60 saniye icinde kabul eden oyuncu Avatar olur.
 */
public final class AvatarCommand implements TabExecutor {

    private static final long REQUEST_TIMEOUT_MILLIS = 60_000L;

    private final ElementBendingPlugin plugin;

    // Ayni anda tek Avatar olabilecegi icin tek bekleyen teklif yeterlidir.
    private UUID pendingFrom;
    private UUID pendingTo;
    private long pendingExpires;

    public AvatarCommand(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "Bu komut yalnızca oyuncular içindir.", NamedTextColor.RED);
            return true;
        }
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (sub) {
            case "transfer" -> handleTransferRequest(player, args);
            case "kabul", "accept" -> handleAccept(player);
            default -> Msg.send(player, "Kullanım: /avatar transfer <oyuncu> | /avatar kabul", NamedTextColor.YELLOW);
        }
        return true;
    }

    private void handleTransferRequest(Player player, String[] args) {
        if (args.length < 2) {
            Msg.send(player, "Kullanım: /avatar transfer <oyuncu>", NamedTextColor.YELLOW);
            return;
        }
        if (plugin.getElementManager().getElement(player.getUniqueId()) != ElementType.AVATAR) {
            Msg.send(player, "Bu komutu yalnızca mevcut Avatar kullanabilir!", NamedTextColor.RED);
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Msg.send(player, "Oyuncu bulunamadı (çevrimiçi olmalı).", NamedTextColor.RED);
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            Msg.send(player, "Avatarlığı kendine devredemezsin!", NamedTextColor.RED);
            return;
        }

        pendingFrom = player.getUniqueId();
        pendingTo = target.getUniqueId();
        pendingExpires = System.currentTimeMillis() + REQUEST_TIMEOUT_MILLIS;

        Msg.send(player, target.getName() + " oyuncusuna devir teklifi gönderildi. 60 saniye içinde kabul etmezse geçersiz olur.", NamedTextColor.YELLOW);
        Msg.send(target, player.getName() + " AVATARLIĞINI sana devretmek istiyor!", NamedTextColor.LIGHT_PURPLE);
        Msg.send(target, "Kabul etmek için 60 saniye içinde şunu yaz: /avatar kabul", NamedTextColor.YELLOW);
        Msg.send(target, "Dikkat: kabul edersen mevcut elementini kaybedersin!", NamedTextColor.RED);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.4f);
    }

    private void handleAccept(Player target) {
        if (pendingTo == null || !pendingTo.equals(target.getUniqueId())) {
            Msg.send(target, "Sana yapılmış bir devir teklifi yok.", NamedTextColor.RED);
            return;
        }
        if (System.currentTimeMillis() > pendingExpires) {
            clearPending();
            Msg.send(target, "Devir teklifinin süresi dolmuş (60 sn).", NamedTextColor.RED);
            return;
        }
        Player from = Bukkit.getPlayer(pendingFrom);
        if (from == null || !from.isOnline()) {
            clearPending();
            Msg.send(target, "Devreden oyuncu artık çevrimiçi değil; teklif geçersiz.", NamedTextColor.RED);
            return;
        }
        if (plugin.getElementManager().getElement(from.getUniqueId()) != ElementType.AVATAR) {
            clearPending();
            Msg.send(target, from.getName() + " artık Avatar değil; teklif geçersiz.", NamedTextColor.RED);
            return;
        }
        clearPending();
        performTransfer(from, target);
    }

    private void clearPending() {
        pendingFrom = null;
        pendingTo = null;
        pendingExpires = 0L;
    }

    private void performTransfer(Player from, Player target) {
        // Eski Avatar: ruh envanterinden alinir, unvani sifirlanir
        removeSouls(from);
        plugin.getElementManager().setElementForce(from.getUniqueId(), null);
        plugin.getCooldownManager().clear(from.getUniqueId());

        // Yeni Avatar: unvan + kilitli ruh
        plugin.getElementManager().setElementForce(target.getUniqueId(), ElementType.AVATAR);
        plugin.getCooldownManager().clear(target.getUniqueId());
        AvatarItem.lockIntoInventory(plugin, target);

        target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 80, 0.6, 1.2, 0.6, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        plugin.getServer().broadcast(Component.text(Lang.t("⬢ " + from.getName() + ", AVATARLIĞINI " + target.getName() + " oyuncusuna devretti!"), NamedTextColor.LIGHT_PURPLE));
        Msg.send(from, "Avatarlığı devrettin. Yeni element seçebilirsin: /element sec", NamedTextColor.YELLOW);
        Msg.send(target, "Artık AVATAR'sın! Ruh envanterine kilitlendi. Güçlerin: Shift + 1-5", NamedTextColor.LIGHT_PURPLE);
    }

    private void removeSouls(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (AvatarItem.isAvatarItem(plugin, contents[i])) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String current = args[0].toLowerCase(Locale.ROOT);
            for (String option : List.of("transfer", "kabul")) {
                if (option.startsWith(current)) {
                    result.add(option);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("transfer")) {
            String current = args[1].toLowerCase(Locale.ROOT);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                    result.add(online.getName());
                }
            }
        }
        return result;
    }
}
