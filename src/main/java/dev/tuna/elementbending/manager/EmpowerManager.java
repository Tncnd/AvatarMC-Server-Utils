package dev.tuna.elementbending.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kaya Yumrugu icin "guclendirilmis sonraki vurus" durumunu tutar.
 */
public final class EmpowerManager {

    private final Map<UUID, Long> armed = new ConcurrentHashMap<>();

    public void arm(UUID uuid, int seconds) {
        armed.put(uuid, System.currentTimeMillis() + seconds * 1000L);
    }

    /**
     * Aktifse tuketir ve true doner.
     */
    public boolean consume(UUID uuid) {
        Long end = armed.remove(uuid);
        return end != null && end > System.currentTimeMillis();
    }
}
