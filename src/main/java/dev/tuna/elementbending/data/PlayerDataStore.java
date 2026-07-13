package dev.tuna.elementbending.data;

import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Element secimlerini ve tus atamalarini YAML dosyasinda saklar.
 */
public final class PlayerDataStore {

    private final ElementBendingPlugin plugin;
    private final File file;
    private final Set<UUID> guiShown = new HashSet<>();

    public PlayerDataStore(ElementBendingPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    /** Otomatik secim menusu bu oyuncuya daha once gosterildi mi? */
    public boolean isGuiShown(UUID uuid) {
        return guiShown.contains(uuid);
    }

    public void markGuiShown(UUID uuid) {
        if (guiShown.add(uuid)) {
            saveAll();
        }
    }

    public void loadAll() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String raw : yaml.getStringList("gui-shown")) {
            UUID uuid = parseUuid(raw);
            if (uuid != null) {
                guiShown.add(uuid);
            }
        }
        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) {
            return;
        }
        for (String key : players.getKeys(false)) {
            UUID uuid = parseUuid(key);
            if (uuid == null) {
                plugin.getLogger().warning("players.yml icinde gecersiz UUID: " + key);
                continue;
            }
            ConfigurationSection section = players.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            ElementType element = ElementType.fromName(section.getString("element"));
            if (element == null) {
                continue;
            }
            plugin.getElementManager().loadElement(uuid, element);

            List<AbilityKey> binds = new ArrayList<>();
            for (String name : section.getStringList("binds")) {
                AbilityKey abilityKey = AbilityKey.fromName(name);
                if (abilityKey != null) {
                    binds.add(abilityKey);
                }
            }
            plugin.getKeybindManager().loadBinds(uuid, element, binds);
        }
        plugin.getLogger().info(plugin.getElementManager().view().size() + " oyuncu verisi yuklendi.");
    }

    public void saveAll() {
        YamlConfiguration yaml = new YamlConfiguration();
        List<String> shownList = new ArrayList<>();
        for (UUID uuid : guiShown) {
            shownList.add(uuid.toString());
        }
        yaml.set("gui-shown", shownList);
        Map<UUID, ElementType> elements = plugin.getElementManager().view();
        for (Map.Entry<UUID, ElementType> entry : elements.entrySet()) {
            String base = "players." + entry.getKey();
            ElementType element = entry.getValue();
            yaml.set(base + ".element", element.name());

            List<String> bindNames = new ArrayList<>();
            for (AbilityKey key : plugin.getKeybindManager().getBinds(entry.getKey(), element)) {
                bindNames.add(key.name());
            }
            yaml.set(base + ".binds", bindNames);
        }
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("players.yml kaydedilemedi: " + ex.getMessage());
        }
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
