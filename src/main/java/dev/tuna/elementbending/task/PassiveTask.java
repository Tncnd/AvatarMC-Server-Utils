package dev.tuna.elementbending.task;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Element pasiflerini duzenli araliklarla uygular.
 * 40 tick'te bir calisir; efektler 90 tick surer (kesintisiz his).
 */
public final class PassiveTask extends BukkitRunnable {

    private static final int EFFECT_TICKS = 90;

    private final ElementBendingPlugin plugin;

    public PassiveTask(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ElementType element = plugin.getElementManager().getElement(player.getUniqueId());
            if (element == null) {
                continue;
            }
            switch (element) {
                case AIR -> effect(player, PotionEffectType.SPEED, 0);
                case FIRE -> effect(player, PotionEffectType.FIRE_RESISTANCE, 0);
                case WATER -> {
                    effect(player, PotionEffectType.WATER_BREATHING, 0);
                    effect(player, PotionEffectType.DOLPHINS_GRACE, 0);
                    if (isInRain(player)) {
                        effect(player, PotionEffectType.REGENERATION, 0);
                    }
                }
                case EARTH -> {
                    effect(player, Compat.RESISTANCE, 0);
                    if (isOnStone(player)) {
                        effect(player, Compat.HASTE, 0);
                    }
                }
                case AVATAR -> {
                    effect(player, PotionEffectType.FIRE_RESISTANCE, 0);
                    effect(player, PotionEffectType.WATER_BREATHING, 0);
                }
            }
        }
    }

    private void effect(Player player, PotionEffectType type, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, EFFECT_TICKS, amplifier, true, false));
    }

    private boolean isInRain(Player player) {
        Location location = player.getLocation();
        return player.getWorld().hasStorm()
                && player.getWorld().getHighestBlockYAt(location) <= location.getBlockY();
    }

    private boolean isOnStone(Player player) {
        Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        if (Tag.BASE_STONE_OVERWORLD.isTagged(below) || Tag.BASE_STONE_NETHER.isTagged(below)) {
            return true;
        }
        String name = below.name();
        return name.contains("STONE") || name.contains("DEEPSLATE")
                || name.contains("ANDESITE") || name.contains("DIORITE")
                || name.contains("GRANITE") || name.contains("TUFF");
    }
}
