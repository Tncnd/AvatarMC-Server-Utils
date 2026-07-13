package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.AvatarItem;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Avatar Ruhu kurallari:
 * - Aktif Avatar ruhu slotundan cikaramaz, atamaz, tasiyamaz.
 * - Avatar oldugunde: katil oyuncuysa ruhu o alir; degilse 100 blok icindeki
 *   en yakin oyuncu alir; kimse yoksa ruh olum noktasina korumali dusurulur.
 * - Dusen ruh despawn olmaz, moblar alamaz, temizleyicilerden korunur.
 */
public final class AvatarSoulListener implements Listener {

    private final ElementBendingPlugin plugin;

    public AvatarSoulListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isSoul(ItemStack item) {
        return AvatarItem.isAvatarItem(plugin, item);
    }

    private boolean isAvatar(Player player) {
        return plugin.getElementManager().getElement(player.getUniqueId()) == ElementType.AVATAR;
    }

    // ---------- Slot kilidi (yalnizca aktif Avatar icin) ----------

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !isAvatar(player)) {
            return;
        }
        ItemStack hotbarItem = event.getHotbarButton() >= 0
                ? player.getInventory().getItem(event.getHotbarButton()) : null;
        if (isSoul(event.getCurrentItem()) || isSoul(event.getCursor()) || isSoul(hotbarItem)) {
            event.setCancelled(true);
            Msg.actionBar(player, "Avatar Ruhu yerinden çıkarılamaz!", NamedTextColor.LIGHT_PURPLE);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !isAvatar(player)) {
            return;
        }
        if (isSoul(event.getOldCursor()) || isSoul(event.getCursor())
                || event.getNewItems().values().stream().anyMatch(this::isSoul)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (isAvatar(event.getPlayer()) && isSoul(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            Msg.actionBar(event.getPlayer(), "Avatar Ruhu atılamaz!", NamedTextColor.LIGHT_PURPLE);
        }
    }

    /** Ruh elde tutulabilse bile item frame/zirh standina yerlestirilemesin. */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!isAvatar(player)) {
            return;
        }
        if (isSoul(player.getInventory().getItemInMainHand())
                || isSoul(player.getInventory().getItemInOffHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (isAvatar(event.getPlayer())
                && (isSoul(event.getMainHandItem()) || isSoul(event.getOffHandItem()))) {
            event.setCancelled(true);
        }
    }

    // ---------- Olum: ruhun el degistirmesi ----------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        if (!isAvatar(dead)) {
            return;
        }

        // Ruh normal esya gibi dusmesin / keepInventory ile uzerinde kalmasin
        event.getDrops().removeIf(this::isSoul);
        ItemStack[] contents = dead.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isSoul(contents[i])) {
                dead.getInventory().setItem(i, null);
            }
        }

        // Avatar unvanini kaybeder (kontenjan bosalir, ruhu alan aktive edebilir)
        plugin.getElementManager().setElementForce(dead.getUniqueId(), null);
        plugin.getCooldownManager().clear(dead.getUniqueId());
        plugin.getServer().broadcast(Component.text(Lang.t("⬢ AVATAR " + dead.getName() + " öldü!"), NamedTextColor.LIGHT_PURPLE));
        Msg.send(dead, "Avatar Ruhu'nu kaybettin. Yeni element seçebilirsin: /element sec", NamedTextColor.YELLOW);

        // 1) Katil oyuncuysa ruhu o alir
        Player killer = dead.getKiller();
        if (killer != null && killer.isOnline() && !killer.getUniqueId().equals(dead.getUniqueId())) {
            plugin.getServer().broadcast(Component.text(Lang.t("⚔ " + killer.getName() + ", AVATAR " + dead.getName() + "'i öldürerek Avatar Ruhu'nu ele geçirdi!"), NamedTextColor.RED));
            giveSoul(killer);
            return;
        }

        // 2) Yaratik/void/cevre olumu: ruh yere duser, ilk alan sahibi olur
        plugin.getSoulItemManager().spawnProtected(dead.getLocation());
    }

    private void giveSoul(Player receiver) {
        for (ItemStack leftover : receiver.getInventory().addItem(AvatarItem.create(plugin)).values()) {
            // Envanter doluysa ayaklarinin dibine korumali dusur
            plugin.getSoulItemManager().spawnProtected(receiver.getLocation());
            return;
        }
        Msg.send(receiver, "Avatar Ruhu'nu ele geçirdin! Sağ tıklayarak Avatar olabilirsin.", NamedTextColor.LIGHT_PURPLE);
    }

    // ---------- Dusen ruhun korunmasi ----------

    /**
     * Guvenlik agi: ruh item'i HERHANGI bir yolla yere duserse
     * (tasiyicinin olumu, Q ile atma, sandik patlamasi, dispenser...)
     * otomatik olarak korumali hale gelir ve takibe alinir.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (isSoul(item.getItemStack()) && !plugin.getSoulItemManager().isTracked(item.getUniqueId())) {
            plugin.getSoulItemManager().track(item);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDespawn(ItemDespawnEvent event) {
        if (isSoul(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMerge(ItemMergeEvent event) {
        if (isSoul(event.getEntity().getItemStack()) || isSoul(event.getTarget().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if (isSoul(event.getItem().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!isSoul(event.getItem().getItemStack())) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        plugin.getSoulItemManager().untrack(event.getItem().getUniqueId());
        Msg.send(player, "Avatar Ruhu'nu aldın! Sağ tıklayarak Avatar olabilirsin.", NamedTextColor.LIGHT_PURPLE);
        plugin.getServer().broadcast(Component.text(Lang.t("✦ " + player.getName() + " yerdeki AVATAR RUHU'nu kaptı! Ruh artık onun!"), NamedTextColor.LIGHT_PURPLE));
    }

    /** Restart sonrasi: chunk yuklenince eski ruh itemlerini tekrar takibe al. */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (org.bukkit.entity.Entity entity : event.getEntities()) {
            if (entity instanceof Item item && isSoul(item.getItemStack())
                    && !plugin.getSoulItemManager().isTracked(item.getUniqueId())) {
                plugin.getSoulItemManager().track(item);
            }
        }
    }
}
