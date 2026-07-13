package dev.tuna.elementbending.command;

import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * /nightvision — sinirsiz Night Vision II acar/kapatir. Herkes kullanabilir.
 */
public final class NightVisionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "Bu komut yalnızca oyuncular içindir.", NamedTextColor.RED);
            return true;
        }
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            Msg.send(player, "Gece görüşü kapatıldı.", NamedTextColor.GRAY);
        } else {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 1, true, false));
            Msg.send(player, "Sınırsız Gece Görüşü II açıldı! Kapatmak için tekrar yaz.", NamedTextColor.GREEN);
        }
        return true;
    }
}
