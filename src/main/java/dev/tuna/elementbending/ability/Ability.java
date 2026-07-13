package dev.tuna.elementbending.ability;

import org.bukkit.entity.Player;

/**
 * Tek bir gucu temsil eder.
 */
public interface Ability {

    AbilityKey key();

    /**
     * Gucu calistirir.
     *
     * @return basariliysa true (bekleme suresi baslatilir), kosul saglanmadiysa false
     */
    boolean execute(Player player);
}
