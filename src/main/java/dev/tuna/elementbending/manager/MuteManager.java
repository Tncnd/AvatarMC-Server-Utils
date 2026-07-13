package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.ElementBendingPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat susturmalarini yonetir ve mutes.yml'de kalici saklar.
 */
public final class MuteManager {

    public record MuteEntry(long endMillis, String reason) {
    }

    private final ElementBendingPlugin plugin;
    private final File file;
    private final Map<UUID, MuteEntry> mutes = new ConcurrentHashMap<>();

    public MuteManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
    }

    public void mute(UUID uuid, long endMillis, String reason) {
        mutes.put(uuid, new MuteEntry(endMillis, reason));
        save();
    }

    public boolean unmute(UUID uuid) {
        boolean removed = mutes.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    /** Suresi dolmus kayitlari temizleyerek kontrol eder. */
    public MuteEntry getActiveMute(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) {
            return null;
        }
        if (entry.endMillis() <= System.currentTimeMillis()) {
            mutes.remove(uuid);
            // Async chat thread'inden cagrilabilir; dosya kaydini ana thread'e aktar
            if (Bukkit.isPrimaryThread()) {
                save();
            } else {
                Bukkit.getScheduler().runTask(plugin, this::save);
            }
            return null;
        }
        return entry;
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("mutes");
        if (section == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long end = section.getLong(key + ".end");
                String reason = section.getString(key + ".reason", "Sebep belirtilmedi");
                if (end > now) {
                    mutes.put(uuid, new MuteEntry(end, reason));
                }
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("mutes.yml icinde gecersiz UUID: " + key);
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
            String base = "mutes." + entry.getKey();
            yaml.set(base + ".end", entry.getValue().endMillis());
            yaml.set(base + ".reason", entry.getValue().reason());
        }
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("mutes.yml kaydedilemedi: " + ex.getMessage());
        }
    }
}
