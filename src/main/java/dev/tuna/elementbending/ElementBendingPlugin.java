package dev.tuna.elementbending;

import dev.tuna.elementbending.ability.AbilityManager;
import dev.tuna.elementbending.command.AdvancedBanCommand;
import dev.tuna.elementbending.command.AnnouncementCommand;
import dev.tuna.elementbending.command.AutoAnnouncementCommand;
import dev.tuna.elementbending.command.AvatarCommand;
import dev.tuna.elementbending.command.ElementCommand;
import dev.tuna.elementbending.command.GuiAcCommand;
import dev.tuna.elementbending.command.GuideCommand;
import dev.tuna.elementbending.command.MuteCommand;
import dev.tuna.elementbending.command.NightVisionCommand;
import dev.tuna.elementbending.data.PlayerDataStore;
import dev.tuna.elementbending.listener.AvatarItemListener;
import dev.tuna.elementbending.listener.AvatarSoulListener;
import dev.tuna.elementbending.listener.ChatListener;
import dev.tuna.elementbending.listener.ConnectionListener;
import dev.tuna.elementbending.listener.DamageListener;
import dev.tuna.elementbending.listener.GuiListener;
import dev.tuna.elementbending.listener.HotbarListener;
import dev.tuna.elementbending.listener.InteractListener;
import dev.tuna.elementbending.manager.AnnouncementManager;
import dev.tuna.elementbending.manager.AvatarStateManager;
import dev.tuna.elementbending.manager.CooldownManager;
import dev.tuna.elementbending.manager.ElementManager;
import dev.tuna.elementbending.manager.EmpowerManager;
import dev.tuna.elementbending.manager.FireUltManager;
import dev.tuna.elementbending.manager.KeybindManager;
import dev.tuna.elementbending.manager.MuteManager;
import dev.tuna.elementbending.manager.SoulItemManager;
import dev.tuna.elementbending.manager.WallManager;
import dev.tuna.elementbending.manager.WeaponDisableManager;
import dev.tuna.elementbending.task.CompassTask;
import dev.tuna.elementbending.task.PassiveTask;
import dev.tuna.elementbending.util.AvatarCompass;
import dev.tuna.elementbending.util.AvatarItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ElementBending — Avatar tarzi, vanilla dostu element bukme sistemi.
 */
public final class ElementBendingPlugin extends JavaPlugin {

    private ElementManager elementManager;
    private CooldownManager cooldownManager;
    private KeybindManager keybindManager;
    private AbilityManager abilityManager;
    private AvatarStateManager avatarStateManager;
    private WallManager wallManager;
    private EmpowerManager empowerManager;
    private WeaponDisableManager weaponDisableManager;
    private FireUltManager fireUltManager;
    private SoulItemManager soulItemManager;
    private MuteManager muteManager;
    private AnnouncementManager announcementManager;
    private PlayerDataStore dataStore;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dev.tuna.elementbending.util.Lang.init(this);

        this.dataStore = new PlayerDataStore(this);
        this.keybindManager = new KeybindManager();
        this.elementManager = new ElementManager(this);
        this.cooldownManager = new CooldownManager();
        this.avatarStateManager = new AvatarStateManager(this);
        this.wallManager = new WallManager(this);
        this.empowerManager = new EmpowerManager();
        this.weaponDisableManager = new WeaponDisableManager();
        this.fireUltManager = new FireUltManager();
        this.soulItemManager = new SoulItemManager(this);
        this.muteManager = new MuteManager(this);
        this.announcementManager = new AnnouncementManager(this);
        this.abilityManager = new AbilityManager(this);

        dataStore.loadAll();
        muteManager.load();
        announcementManager.load();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new DamageListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
        pluginManager.registerEvents(new HotbarListener(this), this);
        pluginManager.registerEvents(new GuiListener(), this);
        pluginManager.registerEvents(new AvatarItemListener(this), this);
        pluginManager.registerEvents(new AvatarSoulListener(this), this);
        pluginManager.registerEvents(new ChatListener(this), this);
        registerKnockbackListenerIfSupported(pluginManager);
        pluginManager.registerEvents(wallManager, this);

        registerAvatarRecipe();

        PluginCommand command = getCommand("element");
        if (command != null) {
            ElementCommand executor = new ElementCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        PluginCommand guiCommand = getCommand("guiac");
        if (guiCommand != null) {
            GuiAcCommand guiExecutor = new GuiAcCommand(this);
            guiCommand.setExecutor(guiExecutor);
            guiCommand.setTabCompleter(guiExecutor);
        }

        PluginCommand guideCommand = getCommand("guide");
        if (guideCommand != null) {
            guideCommand.setExecutor(new GuideCommand(this));
        }

        PluginCommand avatarCommand = getCommand("avatar");
        if (avatarCommand != null) {
            AvatarCommand avatarExecutor = new AvatarCommand(this);
            avatarCommand.setExecutor(avatarExecutor);
            avatarCommand.setTabCompleter(avatarExecutor);
        }

        PluginCommand nightVisionCommand = getCommand("nightvision");
        if (nightVisionCommand != null) {
            nightVisionCommand.setExecutor(new NightVisionCommand());
        }

        PluginCommand banCommand = getCommand("advancedban");
        if (banCommand != null) {
            AdvancedBanCommand banExecutor = new AdvancedBanCommand();
            banCommand.setExecutor(banExecutor);
            banCommand.setTabCompleter(banExecutor);
        }

        PluginCommand announcementCommand = getCommand("announcement");
        if (announcementCommand != null) {
            announcementCommand.setExecutor(new AnnouncementCommand(this));
        }

        PluginCommand autoAnnouncementCommand = getCommand("autoannouncement");
        if (autoAnnouncementCommand != null) {
            AutoAnnouncementCommand autoExecutor = new AutoAnnouncementCommand(this);
            autoAnnouncementCommand.setExecutor(autoExecutor);
            autoAnnouncementCommand.setTabCompleter(autoExecutor);
        }

        MuteCommand muteExecutor = new MuteCommand(this);
        PluginCommand muteCommand = getCommand("mute");
        if (muteCommand != null) {
            muteCommand.setExecutor(muteExecutor);
            muteCommand.setTabCompleter(muteExecutor);
        }
        PluginCommand unmuteCommand = getCommand("unmute");
        if (unmuteCommand != null) {
            unmuteCommand.setExecutor(muteExecutor);
            unmuteCommand.setTabCompleter(muteExecutor);
        }

        new PassiveTask(this).runTaskTimer(this, 40L, 40L);
        new CompassTask(this).runTaskTimer(this, 60L, 60L);
        soulItemManager.startGuardTask();
        announcementManager.startTask();
        getLogger().info("ElementBending aktif!");
    }

    @Override
    public void onDisable() {
        if (wallManager != null) {
            wallManager.revertAll();
        }
        if (dataStore != null) {
            dataStore.saveAll();
        }
        getLogger().info("ElementBending kapatıldı.");
    }

    public ElementManager getElementManager() {
        return elementManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public AvatarStateManager getAvatarStateManager() {
        return avatarStateManager;
    }

    public WallManager getWallManager() {
        return wallManager;
    }

    public EmpowerManager getEmpowerManager() {
        return empowerManager;
    }

    public WeaponDisableManager getWeaponDisableManager() {
        return weaponDisableManager;
    }

    public FireUltManager getFireUltManager() {
        return fireUltManager;
    }

    public SoulItemManager getSoulItemManager() {
        return soulItemManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }

    public PlayerDataStore getDataStore() {
        return dataStore;
    }

    /**
     * Toprak geri tepme azaltmasi icin gereken event eski surumlerde yok;
     * sinifi yalnizca destekleniyorsa yukle ve kaydet.
     */
    private void registerKnockbackListenerIfSupported(PluginManager pluginManager) {
        try {
            Class.forName("io.papermc.paper.event.entity.EntityKnockbackEvent");
            pluginManager.registerEvents(new dev.tuna.elementbending.listener.KnockbackListener(this), this);
        } catch (ClassNotFoundException ex) {
            getLogger().info("Bu surumde EntityKnockbackEvent yok; Toprak geri tepme azaltmasi devre disi.");
        }
    }

    /**
     * Avatar Ruhu craft tarifi:
     *   Tüy      | Netherite | Deniz Kalbi
     *   Netherite| Ejder Yum.| Netherite
     *   Blaze T. | Netherite | Taş
     */
    private void registerAvatarRecipe() {
        NamespacedKey key = new NamespacedKey(this, "avatar_ruhu");
        getServer().removeRecipe(key);
        ShapedRecipe recipe = new ShapedRecipe(key, AvatarItem.create(this));
        recipe.shape("FNH", "NEN", "BNS");
        recipe.setIngredient('F', Material.FEATHER);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA);
        recipe.setIngredient('E', Material.DRAGON_EGG);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('S', Material.STONE);
        getServer().addRecipe(recipe);
        getLogger().info("Avatar Ruhu tarifi kaydedildi.");
        registerCompassRecipe();
    }

    /**
     * Avatar Pusulasi craft tarifi (koseler GUI element simgeleriyle ayni):
     *   Tüy (Hava)         | Elmas  | Blaze Tozu (Ateş)
     *   Elmas              | Pusula | Elmas
     *   Deniz Kalbi (Su)   | Elmas  | Taş (Toprak)
     */
    private void registerCompassRecipe() {
        NamespacedKey key = new NamespacedKey(this, "avatar_pusulasi");
        getServer().removeRecipe(key);
        ShapedRecipe recipe = new ShapedRecipe(key, AvatarCompass.create(this));
        recipe.shape("FDB", "DCD", "HDS");
        recipe.setIngredient('F', Material.FEATHER);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('C', Material.COMPASS);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA);
        recipe.setIngredient('S', Material.STONE);
        getServer().addRecipe(recipe);
        getLogger().info("Avatar Pusulasi tarifi kaydedildi.");
    }
}
