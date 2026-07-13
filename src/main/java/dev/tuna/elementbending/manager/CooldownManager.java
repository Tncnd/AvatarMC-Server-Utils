package dev.tuna.elementbending.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guc ve pasif bekleme surelerini yonetir.
 */
public final class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void set(UUID uuid, String key, double seconds) {
        if (uuid == null || key == null || seconds <= 0) {
            return;
        }
        cooldowns.computeIfAbsent(uuid, u -> new HashMap<>())
                .put(key, System.currentTimeMillis() + (long) (seconds * 1000L));
    }

    public long remainingMillis(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) {
            return 0L;
        }
        Long end = map.get(key);
        if (end == null) {
            return 0L;
        }
        long remaining = end - System.currentTimeMillis();
        if (remaining <= 0L) {
            map.remove(key);
            return 0L;
        }
        return remaining;
    }

    public boolean isReady(UUID uuid, String key) {
        return remainingMillis(uuid, key) <= 0L;
    }

    public int remainingSeconds(UUID uuid, String key) {
        long millis = remainingMillis(uuid, key);
        return millis <= 0L ? 0 : (int) (millis / 1000L) + 1;
    }

    public void clear(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
