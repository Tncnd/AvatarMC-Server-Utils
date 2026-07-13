package dev.tuna.elementbending.ability.fire;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Ates Sicrayisi: ates veya lav uzerindeyken kisa sure Speed II kazandirir.
 */
public final class FireDashAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public FireDashAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.FIRE_DASH;
    }

    @Override
    public boolean execute(Player player) {
        if (!isOnFireGround(player)) {
            Msg.actionBar(player, "Ateş veya lav üzerinde olmalısın!", NamedTextColor.RED);
            return false;
        }
        int seconds = plugin.getConfig().getInt("abilities.fire-dash.speed-seconds", 8);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds * 20, 1, true, true));

        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 25, 0.4, 0.3, 0.4, 0.03);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.4f);
        return true;
    }

    private boolean isOnFireGround(Player player) {
        if (player.isInLava() || player.getFireTicks() > 0) {
            return true;
        }
        Block feet = player.getLocation().getBlock();
        Block below = feet.getRelative(BlockFace.DOWN);
        Material feetType = feet.getType();
        Material belowType = below.getType();
        return feetType == Material.FIRE || feetType == Material.SOUL_FIRE || feetType == Material.LAVA
                || belowType == Material.MAGMA_BLOCK || belowType == Material.FIRE || belowType == Material.SOUL_FIRE;
    }
}
