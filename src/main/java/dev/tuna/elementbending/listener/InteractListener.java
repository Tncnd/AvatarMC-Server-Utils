package dev.tuna.elementbending.listener;

import dev.tuna.elementbending.util.Compat;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

/**
 * Bos elle sag tik davranislari:
 * Hava -> Wind Charge, Ates -> Fire Charge (ult sirasinda Ghast Fireball),
 * Avatar -> normalde Wind Charge, Shift ile Fire Charge.
 */
public final class InteractListener implements Listener {

    private static final String CHARGE_KEY = "CHARGE";

    private final ElementBendingPlugin plugin;

    public InteractListener(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().isAir()) {
            return;
        }
        ElementType element = plugin.getElementManager().getElement(player.getUniqueId());
        if (element == null) {
            return;
        }
        switch (element) {
            case AIR -> shootWindCharge(player);
            case FIRE -> shootFireCharge(player, true);
            case AVATAR -> {
                if (player.isSneaking()) {
                    shootFireCharge(player, false);
                } else {
                    shootWindCharge(player);
                }
            }
            default -> {
            }
        }
    }

    private boolean chargeReady(Player player) {
        if (player.isFrozen()) {
            Msg.actionBar(player, "Donmuş haldeyken güç kullanamazsın!", NamedTextColor.AQUA);
            return false;
        }
        int remaining = plugin.getCooldownManager().remainingSeconds(player.getUniqueId(), CHARGE_KEY);
        if (remaining > 0) {
            Msg.actionBar(player, "⏳ Bekle: " + remaining + " sn", NamedTextColor.GRAY);
            return false;
        }
        plugin.getCooldownManager().set(player.getUniqueId(), CHARGE_KEY,
                plugin.getConfig().getDouble("charge-cooldown-seconds", 2));
        return true;
    }

    private void shootWindCharge(Player player) {
        if (!chargeReady(player)) {
            return;
        }
        Compat.launchWindCharge(player);
        player.getWorld().playSound(player.getLocation(), Compat.SOUND_WIND_THROW, 1.0f, 1.0f);
    }

    /**
     * @param allowUlt Ejderha Alevi aktifse Ghast Fireball'a donusebilir mi
     */
    private void shootFireCharge(Player player, boolean allowUlt) {
        if (!chargeReady(player)) {
            return;
        }
        if (allowUlt && plugin.getFireUltManager().isActive(player.getUniqueId())) {
            LargeFireball fireball = player.launchProjectile(LargeFireball.class,
                    player.getEyeLocation().getDirection().multiply(1.2));
            fireball.setYield((float) plugin.getConfig().getDouble("abilities.dragon-fire.fireball-yield", 1.0));
            fireball.setIsIncendiary(true);
            fireball.getPersistentDataContainer().set(dragonFireballKey(), PersistentDataType.BYTE, (byte) 1);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.9f);
            return;
        }
        player.launchProjectile(SmallFireball.class,
                player.getEyeLocation().getDirection().multiply(1.2));
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
    }

    private NamespacedKey dragonFireballKey() {
        return new NamespacedKey(plugin, "dragon_fireball");
    }

    /**
     * Grief korumasi: Ejderha Alevi fireball'lari varsayilan olarak
     * blok kirmaz (hasar ve itme aynen kalir).
     */
    @EventHandler(ignoreCancelled = true)
    public void onFireballExplode(EntityExplodeEvent event) {
        if (plugin.getConfig().getBoolean("abilities.dragon-fire.break-blocks", true)) {
            return;
        }
        if (event.getEntity().getPersistentDataContainer().has(dragonFireballKey(), PersistentDataType.BYTE)) {
            event.blockList().clear();
        }
    }
}
