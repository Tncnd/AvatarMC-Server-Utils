package dev.tuna.elementbending.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ejderha Alevi ultimatesinin aktiflik suresini tutar.
 */
public final class FireUltManager {

    private final Map<UUID, Long> active = new ConcurrentHashMap<>();

    public void activate(UUID uuid, int seconds) {
        active.put(uuid, System.currentTimeMillis() + seconds * 1000L);
    }

    public boolean isActive(UUID uuid) {
        Long end = active.get(uuid);
        if (end == null) {
            return false;
        }
        if (end <= System.currentTimeMillis()) {
            active.remove(uuid);
            return false;
        }
        return true;
    }
}
