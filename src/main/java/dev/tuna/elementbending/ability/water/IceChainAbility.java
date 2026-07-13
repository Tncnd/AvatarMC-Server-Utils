package dev.tuna.elementbending.ability.water;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.Ability;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Buz Zinciri: bakilan rakibi kisa sure dondurur.
 * Avatar kullanirsa sure daha kisadir.
 */
public final class IceChainAbility implements Ability {

    private final ElementBendingPlugin plugin;
    /** Hedef basina donma bitis zamani; ust uste donmalarda erken cozulmeyi engeller. */
    private final Map<UUID, Long> freezeEnd = new HashMap<>();

    public IceChainAbility(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbilityKey key() {
        return AbilityKey.ICE_CHAIN;
    }

    @Override
    public boolean execute(Player player) {
        double range = plugin.getConfig().getDouble("abilities.ice-chain.range", 12.0);

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                1.0,
                entity -> isTarget(entity, player));

        if (result == null || !(result.getHitEntity() instanceof LivingEntity target)) {
            Msg.actionBar(player, "Hedef bulunamadı!", NamedTextColor.RED);
            return false;
        }

        boolean isAvatar = plugin.getElementManager().getElement(player.getUniqueId()) == ElementType.AVATAR;
        int seconds = isAvatar
                ? plugin.getConfig().getInt("abilities.ice-chain.avatar-freeze-seconds", 2)
                : plugin.getConfig().getInt("abilities.ice-chain.freeze-seconds", 3);
        int ticks = seconds * 20;

        target.addPotionEffect(new PotionEffect(Compat.SLOWNESS, ticks, 250, true, false));
        target.addPotionEffect(new PotionEffect(Compat.MINING_FATIGUE, ticks, 2, true, false));
        target.setFreezeTicks(140);
        target.lockFreezeTicks(true);
        long end = System.currentTimeMillis() + ticks * 50L;
        freezeEnd.put(target.getUniqueId(), end);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Long current = freezeEnd.get(target.getUniqueId());
            if (current == null || current > System.currentTimeMillis()) {
                return; // daha yeni bir donma aktif
            }
            freezeEnd.remove(target.getUniqueId());
            target.lockFreezeTicks(false);
            target.setFreezeTicks(0);
        }, ticks);

        target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.02);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f);
        if (target instanceof Player targetPlayer) {
            Msg.actionBar(targetPlayer, "Donduruldun! (" + seconds + " sn)", NamedTextColor.AQUA);
        }
        Msg.actionBar(player, target.getName() + " donduruldu!", NamedTextColor.AQUA);
        return true;
    }

    private boolean isTarget(Entity entity, Player source) {
        if (!(entity instanceof LivingEntity living) || living instanceof ArmorStand) {
            return false;
        }
        if (living.getUniqueId().equals(source.getUniqueId())) {
            return false;
        }
        if (living instanceof Player target) {
            return target.getGameMode() != org.bukkit.GameMode.SPECTATOR
                    && target.getGameMode() != org.bukkit.GameMode.CREATIVE;
        }
        return true;
    }
}
