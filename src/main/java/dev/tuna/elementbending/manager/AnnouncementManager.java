package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.ElementBendingPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Duyuru sistemi: tek seferlik ve tekrarlayan (otomatik) duyurular.
 * Her duyuru, dikkat cekmesi icin tum oyunculara dusen ors sesi caldirir.
 * Otomatik duyurular announcements.yml'de kalicidir.
 */
public final class AnnouncementManager {

    public record AutoAnnouncement(int id, String message, long intervalMillis) {
    }

    private static final long CHECK_PERIOD_TICKS = 200L; // 10 sn

    private final ElementBendingPlugin plugin;
    private final File file;
    private final Map<Integer, AutoAnnouncement> autos = new TreeMap<>();
    private final Map<Integer, Long> nextRun = new TreeMap<>();
    private int nextId = 1;

    public AnnouncementManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "announcements.yml");
    }

    /** Vurgulu duyuru + tum oyunculara dusen ors sesi. */
    public void broadcast(String message, String author) {
        Component text = Component.text(dev.tuna.elementbending.util.Lang.t("📢 DUYURU » "), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(message, NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, false));
        if (author != null) {
            text = text.append(Component.text("  — " + author, NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false));
        }
        Bukkit.broadcast(text);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        }
    }

    public AutoAnnouncement add(String message, long intervalMillis) {
        AutoAnnouncement entry = new AutoAnnouncement(nextId++, message, intervalMillis);
        autos.put(entry.id(), entry);
        nextRun.put(entry.id(), System.currentTimeMillis() + intervalMillis);
        save();
        return entry;
    }

    public boolean remove(int id) {
        boolean removed = autos.remove(id) != null;
        nextRun.remove(id);
        if (removed) {
            save();
        }
        return removed;
    }

    public Map<Integer, AutoAnnouncement> view() {
        return Map.copyOf(autos);
    }

    public void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, CHECK_PERIOD_TICKS, CHECK_PERIOD_TICKS);
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (AutoAnnouncement entry : autos.values()) {
            Long due = nextRun.get(entry.id());
            if (due == null) {
                nextRun.put(entry.id(), now + entry.intervalMillis());
                continue;
            }
            if (due <= now) {
                broadcast(entry.message(), null);
                nextRun.put(entry.id(), now + entry.intervalMillis());
            }
        }
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("auto");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                String message = section.getString(key + ".message");
                long interval = section.getLong(key + ".interval-millis");
                if (message == null || message.isBlank() || interval < 60_000L) {
                    continue;
                }
                autos.put(id, new AutoAnnouncement(id, message, interval));
                nextRun.put(id, System.currentTimeMillis() + interval);
                nextId = Math.max(nextId, id + 1);
            } catch (NumberFormatException ignored) {
                plugin.getLogger().warning("announcements.yml icinde gecersiz id: " + key);
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (AutoAnnouncement entry : autos.values()) {
            String base = "auto." + entry.id();
            yaml.set(base + ".message", entry.message());
            yaml.set(base + ".interval-millis", entry.intervalMillis());
        }
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("announcements.yml kaydedilemedi: " + ex.getMessage());
        }
    }
}
