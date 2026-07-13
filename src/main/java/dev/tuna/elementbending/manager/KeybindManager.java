package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Oyuncu basina "Shift+N" slotlarina atanan gucleri yonetir.
 * Shift zorunlulugu sabittir; yalnizca slot sirasi degistirilebilir.
 */
public final class KeybindManager {

    private final Map<UUID, List<AbilityKey>> binds = new HashMap<>();

    /**
     * Elemente gore izin verilen guclerin varsayilan sirasi.
     */
    public static List<AbilityKey> defaultBinds(ElementType type) {
        return switch (type) {
            case AIR -> List.of(AbilityKey.AIR_JUMP, AbilityKey.AIR_BLAST, AbilityKey.TORNADO);
            case FIRE -> List.of(AbilityKey.FLAME_WAVE, AbilityKey.FIRE_DASH, AbilityKey.DRAGON_FIRE);
            case WATER -> List.of(AbilityKey.WATER_PUSH, AbilityKey.ICE_CHAIN, AbilityKey.HEALING_WAVE);
            case EARTH -> List.of(AbilityKey.ROCK_PUNCH, AbilityKey.STONE_WALL, AbilityKey.EARTHQUAKE);
            case AVATAR -> List.of(AbilityKey.STONE_WALL, AbilityKey.ICE_CHAIN,
                    AbilityKey.FLAME_WAVE, AbilityKey.AIR_JUMP, AbilityKey.AVATAR_STATE);
        };
    }

    public List<AbilityKey> getBinds(UUID uuid, ElementType type) {
        List<AbilityKey> allowed = defaultBinds(type);
        List<AbilityKey> list = binds.get(uuid);
        // Icerik dogrulamasi: liste tam olarak bu elementin guclerinden olusmali.
        // (Element degisiminden kalan eski binds ile baska elementin gucleri kullanilamasin.)
        if (list == null || list.size() != allowed.size() || !list.containsAll(allowed)) {
            list = new ArrayList<>(allowed);
            binds.put(uuid, list);
        }
        return list;
    }

    public List<AbilityKey> viewBinds(UUID uuid, ElementType type) {
        return Collections.unmodifiableList(getBinds(uuid, type));
    }

    /**
     * @param slot hotbar slot indeksi (0 tabanli; Shift+1 -> 0)
     */
    public AbilityKey getBind(UUID uuid, ElementType type, int slot) {
        List<AbilityKey> list = getBinds(uuid, type);
        if (slot < 0 || slot >= list.size()) {
            return null;
        }
        return list.get(slot);
    }

    /**
     * Slottaki gucu, izin verilen listede bir sonraki guce cevirir.
     * Cakismayi onlemek icin diger slotla takas yapilir.
     */
    public void cycle(UUID uuid, ElementType type, int slot) {
        List<AbilityKey> list = getBinds(uuid, type);
        if (slot < 0 || slot >= list.size()) {
            return;
        }
        List<AbilityKey> allowed = defaultBinds(type);
        AbilityKey current = list.get(slot);
        AbilityKey next = allowed.get((allowed.indexOf(current) + 1) % allowed.size());
        int otherSlot = list.indexOf(next);
        list.set(slot, next);
        if (otherSlot >= 0 && otherSlot != slot) {
            list.set(otherSlot, current);
        }
    }

    /** Veri yuklemesi icin. Gecersiz listeler yok sayilir. */
    public void loadBinds(UUID uuid, ElementType type, List<AbilityKey> loaded) {
        if (uuid == null || type == null || loaded == null) {
            return;
        }
        List<AbilityKey> allowed = defaultBinds(type);
        if (loaded.size() != allowed.size() || !loaded.containsAll(allowed)) {
            return;
        }
        binds.put(uuid, new ArrayList<>(loaded));
    }

    public void reset(UUID uuid) {
        binds.remove(uuid);
    }

    public Map<UUID, List<AbilityKey>> view() {
        return Collections.unmodifiableMap(binds);
    }
}
