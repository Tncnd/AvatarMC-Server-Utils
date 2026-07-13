package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.AvatarItem;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Avatar Ruhu iteminin kullanimi ve craft kontrolu.
 */
public final class AvatarItemListener implements Listener {

    private final ElementBendingPlugin plugin;

    public AvatarItemListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!AvatarItem.isAvatarItem(plugin, item)) {
            return;
        }
        event.setCancelled(true);

        if (plugin.getElementManager().getElement(player.getUniqueId()) == ElementType.AVATAR) {
            Msg.send(player, "Zaten Avatar'sın!", NamedTextColor.RED);
            return;
        }
        if (plugin.getElementManager().isFull(ElementType.AVATAR)) {
            Msg.send(player, "Sunucuda zaten bir Avatar var!", NamedTextColor.RED);
            return;
        }

        // Ruh tuketilmez: ozel slota (envanter sag alt kose) kilitlenir.
        item.setAmount(item.getAmount() - 1);
        AvatarItem.lockIntoInventory(plugin, player);

        plugin.getElementManager().setElementForce(player.getUniqueId(), ElementType.AVATAR);
        plugin.getCooldownManager().clear(player.getUniqueId());

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 80, 0.6, 1.2, 0.6, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        plugin.getServer().broadcast(Component.text(Lang.t("⬢ " + player.getName() + " yeni AVATAR oldu!"), NamedTextColor.LIGHT_PURPLE));
        Msg.send(player, "Avatar Ruhu envanterine kilitlendi — çıkarılamaz, ölürsen el değiştirir!", NamedTextColor.LIGHT_PURPLE);
        Msg.send(player, "Güçlerin: Shift + 1-5. Tuş düzeni: /element tus", NamedTextColor.LIGHT_PURPLE);
    }

    /**
     * Ruh craftlandiginda ozel duyuru.
     */
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe() == null || !AvatarItem.isAvatarItem(plugin, event.getRecipe().getResult())) {
            return;
        }
        if (event.getWhoClicked() instanceof Player crafter) {
            plugin.getServer().broadcast(Component.text(Lang.t("⚒ " + crafter.getName() + " AVATAR RUHU'nu craftladı! Sağ tıklarsa yeni Avatar olacak!"), NamedTextColor.GOLD));
        }
    }

    /**
     * Kontenjan doluysa craft sonucunu gizler.
     */
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || !AvatarItem.isAvatarItem(plugin, result)) {
            return;
        }
        if (plugin.getElementManager().isFull(ElementType.AVATAR)) {
            event.getInventory().setResult(null);
        }
    }
}
