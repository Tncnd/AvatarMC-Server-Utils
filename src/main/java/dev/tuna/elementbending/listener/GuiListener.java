package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.gui.ElementSelectGui;
import dev.tuna.elementbending.gui.GuideGui;
import dev.tuna.elementbending.gui.KeybindGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * GUI tiklamalarini yakalar; item alinmasini engeller.
 */
public final class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof ElementSelectGui) && !(holder instanceof KeybindGui)
                && !(holder instanceof GuideGui)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // Yalnizca ust envanterdeki tiklamalar islenir
        if (event.getClickedInventory() != event.getInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (holder instanceof ElementSelectGui gui) {
            gui.handleClick(player, slot);
        } else if (holder instanceof KeybindGui gui) {
            gui.handleClick(player, slot);
        } else if (holder instanceof GuideGui gui) {
            gui.handleClick(player, slot);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ElementSelectGui || holder instanceof KeybindGui
                || holder instanceof GuideGui) {
            event.setCancelled(true);
        }
    }
}
