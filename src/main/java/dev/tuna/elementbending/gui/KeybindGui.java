package dev.tuna.elementbending.gui;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tus atama menusu. Shift zorunlulugu sabittir;
 * yalnizca hangi gucun hangi slotta oldugu degistirilebilir.
 */
public final class KeybindGui implements InventoryHolder {

    private static final int[] SLOTS_3 = {11, 13, 15};
    private static final int[] SLOTS_5 = {9, 11, 13, 15, 17};

    private final ElementBendingPlugin plugin;
    private final UUID owner;
    private final ElementType element;
    private final Inventory inventory;
    private final int[] slots;

    public KeybindGui(ElementBendingPlugin plugin, Player player, ElementType element) {
        this.plugin = plugin;
        this.owner = player.getUniqueId();
        this.element = element;
        this.slots = element == ElementType.AVATAR ? SLOTS_5 : SLOTS_3;
        this.inventory = Bukkit.createInventory(this, 27, Component.text(Lang.t("Tuş Ayarları"), NamedTextColor.GOLD));
        render();
    }

    private void render() {
        List<AbilityKey> binds = plugin.getKeybindManager().getBinds(owner, element);
        for (int i = 0; i < slots.length && i < binds.size(); i++) {
            AbilityKey key = binds.get(i);
            ItemStack item = new ItemStack(key.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Msg.item("Shift+" + (i + 1) + " → " + key.getDisplayName(), key.getElement().getColor())
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Msg.item(key.getDescription(), NamedTextColor.GRAY));
            lore.add(Msg.item("Bekleme: " + plugin.getConfig().getInt("cooldowns." + key.name(),
                    key.getDefaultCooldownSeconds()) + " sn", NamedTextColor.DARK_GRAY));
            lore.add(Msg.item(" ", NamedTextColor.GRAY));
            lore.add(Msg.item("▶ Tıkla: sıradaki güce geç (takas yapılır)", NamedTextColor.YELLOW));
            meta.lore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slots[i], item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void handleClick(Player player, int clickedSlot) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == clickedSlot) {
                plugin.getKeybindManager().cycle(owner, element, i);
                plugin.getDataStore().saveAll();
                render();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                return;
            }
        }
    }
}
