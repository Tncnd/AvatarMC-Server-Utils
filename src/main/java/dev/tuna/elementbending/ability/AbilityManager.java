package dev.tuna.elementbending.ability;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.air.AirBlastAbility;
import dev.tuna.elementbending.ability.air.AirJumpAbility;
import dev.tuna.elementbending.ability.air.TornadoAbility;
import dev.tuna.elementbending.ability.avatar.AvatarStateAbility;
import dev.tuna.elementbending.ability.earth.EarthquakeAbility;
import dev.tuna.elementbending.ability.earth.RockPunchAbility;
import dev.tuna.elementbending.ability.earth.StoneWallAbility;
import dev.tuna.elementbending.ability.fire.DragonFireAbility;
import dev.tuna.elementbending.ability.fire.FireDashAbility;
import dev.tuna.elementbending.ability.fire.FlameWaveAbility;
import dev.tuna.elementbending.ability.water.HealingWaveAbility;
import dev.tuna.elementbending.ability.water.IceChainAbility;
import dev.tuna.elementbending.ability.water.WaterPushAbility;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tum gucleri kaydeder; bekleme suresi kontrolu ve calistirmayi yonetir.
 */
public final class AbilityManager {

    private final ElementBendingPlugin plugin;
    private final Map<AbilityKey, Ability> abilities = new EnumMap<>(AbilityKey.class);

    public AbilityManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
        register(new AirJumpAbility(plugin));
        register(new AirBlastAbility(plugin));
        register(new TornadoAbility(plugin));
        register(new FlameWaveAbility(plugin));
        register(new FireDashAbility(plugin));
        register(new DragonFireAbility(plugin));
        register(new WaterPushAbility(plugin));
        register(new IceChainAbility(plugin));
        register(new HealingWaveAbility(plugin));
        register(new RockPunchAbility(plugin));
        register(new StoneWallAbility(plugin));
        register(new EarthquakeAbility(plugin));
        register(new AvatarStateAbility(plugin));
    }

    private void register(Ability ability) {
        abilities.put(ability.key(), ability);
    }

    public void tryUse(Player player, AbilityKey key) {
        Ability ability = abilities.get(key);
        if (ability == null) {
            return;
        }
        UUID uuid = player.getUniqueId();

        // Donmus oyuncu guc kullanamaz (Buz Zinciri'nden kacis engeli)
        if (player.isFrozen()) {
            Msg.actionBar(player, "Donmuş haldeyken güç kullanamazsın!", NamedTextColor.AQUA);
            return;
        }

        int remaining = plugin.getCooldownManager().remainingSeconds(uuid, key.name());
        if (remaining > 0) {
            Msg.actionBar(player, "⏳ " + key.getDisplayName() + ": " + remaining + " sn", NamedTextColor.RED);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.7f);
            return;
        }

        if (!ability.execute(player)) {
            return;
        }

        double seconds = plugin.getConfig().getDouble("cooldowns." + key.name(), key.getDefaultCooldownSeconds());
        // Avatar State aktifken bekleme sureleri azalir; kendi cooldown'u haric.
        if (key != AbilityKey.AVATAR_STATE && plugin.getAvatarStateManager().isActive(uuid)) {
            seconds *= plugin.getAvatarStateManager().cooldownMultiplier();
        }
        plugin.getCooldownManager().set(uuid, key.name(), seconds);
        Msg.actionBar(player, "✦ " + key.getDisplayName() + " kullanıldı", key.getElement().getColor());
    }
}
