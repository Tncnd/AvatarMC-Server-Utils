package dev.tuna.elementbending.ability.earth;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Tas Duvar: oyuncunun onune gecici bir tas duvar orer.
 * Blok yerlestirme, koruma ve geri alma islemleri WallManager'da yapilir.
 */
public final class StoneWallAbility implements Ability {

    private final ElementBendingPlugin plugin;

    public StoneWallAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.STONE_WALL;
    }

    @Override
    public boolean execute(Player player) {
        if (!plugin.getWallManager().buildWall(player)) {
            Msg.actionBar(player, "Duvar için yer yok!", NamedTextColor.RED);
            return false;
        }
        return true;
    }
}
