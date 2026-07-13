package dev.tuna.elementbending.gui;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.manager.ElementManager;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import dev.tuna.elementbending.element.ElementType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Element secim menusu. Secim kalicidir.
 */
public final class ElementSelectGui implements InventoryHolder {

    private static final int[] SLOTS = {10, 12, 14, 16};
    private static final ElementType[] ORDER = {
            ElementType.AIR, ElementType.WATER, ElementType.FIRE, ElementType.EARTH
    };
    private static final int AVATAR_INFO_SLOT = 22;

    private final ElementBendingPlugin plugin;
    private final Inventory inventory;

    public ElementSelectGui(ElementBendingPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, Component.text(Lang.t("Element Seçimi"), NamedTextColor.GOLD));
        render();
    }

    private void render() {
        for (int i = 0; i < ORDER.length; i++) {
            ElementType type = ORDER[i];
            int count = plugin.getElementManager().count(type);
            int max = plugin.getElementManager().maxFor(type);
            boolean full = count >= max;

            ItemStack item = new ItemStack(type.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Msg.item(type.getDisplayName(), type.getColor())
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Msg.item("Seçilen: " + count + "/" + max, NamedTextColor.GRAY));
            lore.add(Msg.item(" ", NamedTextColor.GRAY));
            if (full) {
                lore.add(Msg.item("✖ DOLU", NamedTextColor.RED));
            } else {
                lore.add(Msg.item("✔ Seçmek için tıkla", NamedTextColor.GREEN));
            }
            lore.add(Msg.item("Seçim kalıcıdır!", NamedTextColor.DARK_RED));
            meta.lore(lore);
            item.setItemMeta(meta);
            inventory.setItem(SLOTS[i], item);
        }
        renderAvatarInfo();
    }

    /** Avatar secilemez; craft ile elde edilir. Bilgi itemi. */
    private void renderAvatarInfo() {
        ElementType type = ElementType.AVATAR;
        int count = plugin.getElementManager().count(type);
        int max = plugin.getElementManager().maxFor(type);

        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Msg.item(type.getDisplayName(), type.getColor())
                .decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(Msg.item("Seçilemez — craft ile elde edilir!", NamedTextColor.YELLOW));
        lore.add(Msg.item(" ", NamedTextColor.GRAY));
        lore.add(Msg.item("Tarif (3x3):", NamedTextColor.GRAY));
        lore.add(Msg.item("Tüy | Netherite | Deniz Kalbi", NamedTextColor.DARK_GRAY));
        lore.add(Msg.item("Netherite | Ejderha Yumurtası | Netherite", NamedTextColor.DARK_GRAY));
        lore.add(Msg.item("Blaze Tozu | Netherite | Taş", NamedTextColor.DARK_GRAY));
        lore.add(Msg.item(" ", NamedTextColor.GRAY));
        lore.add(Msg.item("Durum: " + count + "/" + max + (count >= max ? " (DOLU)" : " (BOŞ)"), NamedTextColor.GRAY));
        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(AVATAR_INFO_SLOT, item);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void handleClick(Player player, int slot) {
        ElementType clicked = null;
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                clicked = ORDER[i];
                break;
            }
        }
        if (clicked == null) {
            if (slot == AVATAR_INFO_SLOT) {
                Msg.send(player, "Avatar seçilemez! Avatar Ruhu itemini craftlayıp sağ tıklamalısın.", NamedTextColor.LIGHT_PURPLE);
            }
            return;
        }

        ElementManager.ChooseResult result = plugin.getElementManager().choose(player.getUniqueId(), clicked);
        switch (result) {
            case ALREADY_CHOSEN -> {
                Msg.send(player, "Zaten bir element seçtin. Seçimler kalıcıdır!", NamedTextColor.RED);
                player.closeInventory();
            }
            case ELEMENT_FULL -> {
                Msg.send(player, clicked.getDisplayName() + " kontenjanı dolu!", NamedTextColor.RED);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);
                render();
            }
            case SUCCESS -> {
                player.closeInventory();
                Msg.send(player, clicked.getDisplayName() + " oldun! Güçlerin: Shift + 1-2-3"
                        + (clicked == ElementType.AVATAR ? "-4-5" : "") + " tuşları.", clicked.getColor());
                Msg.send(player, "Tuş düzenini değiştirmek için: /element tus", NamedTextColor.GRAY);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }
        }
    }
}
