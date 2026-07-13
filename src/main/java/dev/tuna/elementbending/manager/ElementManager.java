package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Oyuncularin element secimlerini ve kontenjanlari yonetir.
 * Secimler kalicidir; cevrimdisi oyuncular da sayilir.
 */
public final class ElementManager {

    public enum ChooseResult {
        SUCCESS,
        ALREADY_CHOSEN,
        ELEMENT_FULL
    }

    private final ElementBendingPlugin plugin;
    private final Map<UUID, ElementType> elements = new HashMap<>();

    public ElementManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    public ElementType getElement(UUID uuid) {
        return elements.get(uuid);
    }

    public boolean hasElement(UUID uuid) {
        return elements.containsKey(uuid);
    }

    public int count(ElementType type) {
        int count = 0;
        for (ElementType value : elements.values()) {
            if (value == type) {
                count++;
            }
        }
        return count;
    }

    public int maxFor(ElementType type) {
        if (type == ElementType.AVATAR) {
            return plugin.getConfig().getInt("limits.avatar", 1);
        }
        return plugin.getConfig().getInt("limits.per-element", 4);
    }

    public boolean isFull(ElementType type) {
        return count(type) >= maxFor(type);
    }

    /**
     * Normal oyuncu secimi: kalicilik ve kontenjan kurallarini uygular.
     */
    public ChooseResult choose(UUID uuid, ElementType type) {
        if (elements.containsKey(uuid)) {
            return ChooseResult.ALREADY_CHOSEN;
        }
        if (isFull(type)) {
            return ChooseResult.ELEMENT_FULL;
        }
        elements.put(uuid, type);
        plugin.getKeybindManager().reset(uuid);
        plugin.getDataStore().saveAll();
        return ChooseResult.SUCCESS;
    }

    /**
     * Admin komutlari icin zorla atama. type null ise secim sifirlanir.
     */
    public void setElementForce(UUID uuid, ElementType type) {
        if (type == null) {
            elements.remove(uuid);
        } else {
            elements.put(uuid, type);
        }
        plugin.getKeybindManager().reset(uuid);
        plugin.getDataStore().saveAll();
    }

    /** Veri yuklemesi icin — kontenjan kontrolu yapilmaz. */
    public void loadElement(UUID uuid, ElementType type) {
        if (uuid != null && type != null) {
            elements.put(uuid, type);
        }
    }

    public Map<UUID, ElementType> view() {
        return Collections.unmodifiableMap(elements);
    }
}
