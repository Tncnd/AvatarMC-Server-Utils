package dev.tuna.elementbending.util;

import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Guc hedefleme yardimcilari.
 */
public final class AbilityUtil {

    private AbilityUtil() {
    }

    private static boolean isValidTarget(Entity entity, Player source) {
        if (!(entity instanceof Player target)) {
            return false;
        }
        if (target.getUniqueId().equals(source.getUniqueId())) {
            return false;
        }
        GameMode mode = target.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    /**
     * Kaynagin etrafindaki (kure) oyuncular.
     *
     * @param requireLineOfSight true ise duvar arkasindaki hedefler elenir
     */
    public static List<Player> playersAround(Player source, double radius, boolean requireLineOfSight) {
        List<Player> result = new ArrayList<>();
        double radiusSq = radius * radius;
        for (Entity entity : source.getNearbyEntities(radius, radius, radius)) {
            if (!isValidTarget(entity, source)) {
                continue;
            }
            if (entity.getLocation().distanceSquared(source.getLocation()) > radiusSq) {
                continue;
            }
            if (requireLineOfSight && !source.hasLineOfSight(entity)) {
                continue;
            }
            result.add((Player) entity);
        }
        return result;
    }

    public static List<Player> playersAround(Player source, double radius) {
        return playersAround(source, radius, false);
    }

    /**
     * Kaynagin baktigi yondeki koni icinde kalan oyuncular.
     *
     * @param minDot bakis yonu ile hedef yonu arasindaki minimum nokta carpimi (0-1)
     */
    public static List<Player> playersInCone(Player source, double range, double minDot) {
        List<Player> result = new ArrayList<>();
        Vector direction = source.getEyeLocation().getDirection().normalize();
        for (Player target : playersAround(source, range, true)) {
            Vector toTarget = target.getLocation().toVector().subtract(source.getLocation().toVector());
            if (toTarget.lengthSquared() < 0.01) {
                result.add(target);
                continue;
            }
            if (toTarget.normalize().dot(direction) >= minDot) {
                result.add(target);
            }
        }
        return result;
    }

    private static boolean isValidLiving(Entity entity, Player source) {
        if (!(entity instanceof LivingEntity living) || living instanceof ArmorStand) {
            return false;
        }
        if (living.getUniqueId().equals(source.getUniqueId())) {
            return false;
        }
        if (living instanceof Player player) {
            GameMode mode = player.getGameMode();
            return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
        }
        return true;
    }

    /**
     * Kaynagin etrafindaki (kure) TUM canlilar (oyuncular, moblar, hayvanlar).
     * Zirh standlari haric.
     */
    public static List<LivingEntity> livingAround(Player source, double radius, boolean requireLineOfSight) {
        List<LivingEntity> result = new ArrayList<>();
        double radiusSq = radius * radius;
        for (Entity entity : source.getNearbyEntities(radius, radius, radius)) {
            if (!isValidLiving(entity, source)) {
                continue;
            }
            if (entity.getLocation().distanceSquared(source.getLocation()) > radiusSq) {
                continue;
            }
            if (requireLineOfSight && !source.hasLineOfSight(entity)) {
                continue;
            }
            result.add((LivingEntity) entity);
        }
        return result;
    }

    /**
     * Kaynagin baktigi yondeki koni icinde kalan TUM canlilar
     * (oyuncular, moblar, hayvanlar). Zirh standlari haric.
     */
    public static List<LivingEntity> livingInCone(Player source, double range, double minDot) {
        List<LivingEntity> result = new ArrayList<>();
        Vector direction = source.getEyeLocation().getDirection().normalize();
        double rangeSq = range * range;
        for (Entity entity : source.getNearbyEntities(range, range, range)) {
            if (!isValidLiving(entity, source)) {
                continue;
            }
            LivingEntity living = (LivingEntity) entity;
            if (living.getLocation().distanceSquared(source.getLocation()) > rangeSq) {
                continue;
            }
            if (!source.hasLineOfSight(living)) {
                continue;
            }
            Vector toTarget = living.getLocation().toVector().subtract(source.getLocation().toVector());
            if (toTarget.lengthSquared() < 0.01 || toTarget.normalize().dot(direction) >= minDot) {
                result.add(living);
            }
        }
        return result;
    }

    /**
     * Y ekseni sifirlanmis, guvenli sekilde normalize edilmis yon vektoru.
     */
    public static Vector horizontalDirection(Player player) {
        Vector direction = player.getLocation().getDirection();
        direction.setY(0);
        if (direction.lengthSquared() > 1.0E-4) {
            direction.normalize();
        } else {
            direction.zero();
        }
        return direction;
    }
}
