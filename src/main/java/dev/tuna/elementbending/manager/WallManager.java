package dev.tuna.elementbending.manager;

import dev.tuna.elementbending.ElementBendingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tas Duvar gucunun gecici bloklarini yerlestirir, korur ve geri alir.
 */
public final class WallManager implements Listener {

    private static final int WALL_HALF_WIDTH = 2;
    private static final int WALL_HEIGHT = 3;

    private final ElementBendingPlugin plugin;
    private final Set<Location> tempBlocks = Collections.synchronizedSet(new HashSet<>());

    public WallManager(ElementBendingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncunun onune gecici tas duvar orer.
     *
     * @return en az bir blok yerlestirildiyse true
     */
    public boolean buildWall(Player player) {
        BlockFace facing = yawToFace(player.getLocation().getYaw());
        BlockFace right = rotateRight(facing);
        Block base = player.getLocation().getBlock().getRelative(facing, 2);

        List<Location> placed = new ArrayList<>();
        for (int w = -WALL_HALF_WIDTH; w <= WALL_HALF_WIDTH; w++) {
            Block column = base.getRelative(right, w);
            for (int h = 0; h < WALL_HEIGHT; h++) {
                Block block = column.getRelative(BlockFace.UP, h);
                if (!block.getType().isAir()) {
                    continue;
                }
                block.setType(Material.STONE, false);
                Location location = block.getLocation();
                tempBlocks.add(location);
                placed.add(location);
            }
        }

        if (placed.isEmpty()) {
            return false;
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.7f);
        int duration = plugin.getConfig().getInt("abilities.stone-wall.duration-seconds", 6);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> revert(placed), duration * 20L);
        return true;
    }

    private void revert(List<Location> locations) {
        for (Location location : locations) {
            if (tempBlocks.remove(location) && location.getBlock().getType() == Material.STONE) {
                location.getBlock().setType(Material.AIR, false);
            }
        }
    }

    /** Plugin kapanirken tum gecici bloklari geri alir. */
    public void revertAll() {
        synchronized (tempBlocks) {
            for (Location location : tempBlocks) {
                if (location.getBlock().getType() == Material.STONE) {
                    location.getBlock().setType(Material.AIR, false);
                }
            }
            tempBlocks.clear();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (tempBlocks.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> tempBlocks.contains(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> tempBlocks.contains(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (tempBlocks.contains(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (tempBlocks.contains(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private static BlockFace yawToFace(float yaw) {
        float normalized = (yaw % 360 + 360) % 360;
        if (normalized >= 315 || normalized < 45) {
            return BlockFace.SOUTH;
        }
        if (normalized < 135) {
            return BlockFace.WEST;
        }
        if (normalized < 225) {
            return BlockFace.NORTH;
        }
        return BlockFace.EAST;
    }

    private static BlockFace rotateRight(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            default -> BlockFace.NORTH;
        };
    }
}
