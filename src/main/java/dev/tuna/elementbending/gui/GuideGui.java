package dev.tuna.elementbending.gui;

import dev.tuna.elementbending.util.Lang;
import dev.tuna.elementbending.ElementBendingPlugin;
import dev.tuna.elementbending.ability.AbilityKey;
import dev.tuna.elementbending.element.ElementType;
import dev.tuna.elementbending.manager.KeybindManager;
import dev.tuna.elementbending.util.Msg;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * /guide menusu: element rehberleri + komut kitabi.
 */
public final class GuideGui implements InventoryHolder {

    private static final int COMMANDS_SLOT = 4;
    private static final int[] ELEMENT_SLOTS = {10, 12, 14, 16, 22};
    private static final ElementType[] ORDER = {
            ElementType.AIR, ElementType.WATER, ElementType.FIRE, ElementType.EARTH, ElementType.AVATAR
    };

    private final ElementBendingPlugin plugin;
    private final Inventory inventory;

    public GuideGui(ElementBendingPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, Component.text(Lang.t("Rehber"), NamedTextColor.GOLD));
        render();
    }

    private void render() {
        // Komut kitabi
        ItemStack book = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.displayName(Msg.item("Komutlar", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true));
        bookMeta.lore(List.of(Msg.item("Tüm komutları görmek için tıkla", NamedTextColor.GRAY)));
        book.setItemMeta(bookMeta);
        inventory.setItem(COMMANDS_SLOT, book);

        // Element rehberleri
        for (int i = 0; i < ORDER.length; i++) {
            ElementType type = ORDER[i];
            ItemStack item = new ItemStack(type.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Msg.item(type.getDisplayName() + " Rehberi", type.getColor())
                    .decoration(TextDecoration.BOLD, true));
            meta.lore(List.of(Msg.item("Pasifler ve güçler için tıkla", NamedTextColor.GRAY)));
            item.setItemMeta(meta);
            inventory.setItem(ELEMENT_SLOTS[i], item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void handleClick(Player player, int slot) {
        Book bookToOpen = null;
        if (slot == COMMANDS_SLOT) {
            bookToOpen = buildCommandsBook();
        } else {
            for (int i = 0; i < ELEMENT_SLOTS.length; i++) {
                if (ELEMENT_SLOTS[i] == slot) {
                    bookToOpen = buildElementBook(ORDER[i]);
                    break;
                }
            }
        }
        if (bookToOpen == null) {
            return;
        }
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        Book finalBook = bookToOpen;
        Bukkit.getScheduler().runTask(plugin, () -> player.openBook(finalBook));
    }

    // ---------- Kitap icerikleri ----------

    private int cooldownOf(AbilityKey key) {
        return plugin.getConfig().getInt("cooldowns." + key.name(), key.getDefaultCooldownSeconds());
    }

    private Book buildElementBook(ElementType type) {
        List<Component> pages = new ArrayList<>();

        // Sayfa 1: pasifler
        Component passives = Component.text(type.getDisplayName() + "\n\n", type.getColor(), TextDecoration.BOLD)
                .append(Component.text(Lang.t("Pasifler:\n"), NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                        .decoration(TextDecoration.BOLD, true))
                .append(Component.text(passivesText(type), NamedTextColor.BLACK)
                        .decoration(TextDecoration.BOLD, false));
        pages.add(passives);

        // Sonraki sayfalar: gucler (varsayilan Shift sirasiyla)
        List<AbilityKey> binds = KeybindManager.defaultBinds(type);
        for (int i = 0; i < binds.size(); i++) {
            AbilityKey key = binds.get(i);
            Component page = Component.text("Shift+" + (i + 1) + "\n", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                    .append(Component.text(key.getDisplayName() + "\n\n", type.getColor(), TextDecoration.BOLD))
                    .append(Component.text(key.getDescription() + "\n\n", NamedTextColor.BLACK)
                            .decoration(TextDecoration.BOLD, false))
                    .append(Component.text(Lang.t("Bekleme: " + cooldownOf(key) + " sn"), NamedTextColor.DARK_RED)
                            .decoration(TextDecoration.BOLD, false));
            pages.add(page);
        }

        if (type == ElementType.AVATAR) {
            pages.add(Component.text(Lang.t("Avatar Ruhu\n\n"), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
                    .append(Component.text(Lang.t("Avatar seçilemez; Avatar Ruhu craftlanıp sağ tıklanarak olunur. "
                                    + "Ruh envanterine kilitlenir. Avatar ölürse: katil oyuncuysa ruhu o alır, "
                                    + "değilse ruh yere düşer ve ilk alan sahibi olur. "
                                    + "Avatar Pusulası yerdeki ruhu gösterir."),
                            NamedTextColor.BLACK).decoration(TextDecoration.BOLD, false)));
        }

        return Book.book(Component.text(Lang.t(type.getDisplayName() + " Rehberi")),
                Component.text("ElementBending"), pages);
    }

    private String passivesText(ElementType type) {
        return Lang.t(switch (type) {
            case AIR -> "• Speed I\n• Düşme hasarı yok\n• Boş elle sağ tık: Wind Charge (2 sn)";
            case WATER -> "• Water Breathing\n• Dolphin's Grace\n• Yağmurda Regeneration I";
            case FIRE -> "• Fire Resistance\n• Boş elle sağ tık: Fire Charge (2 sn)\n• Yumruklar 3 sn yakar";
            case EARTH -> "• Resistance I\n• %50 geri tepme azaltma\n• Taş üzerinde Haste I";
            case AVATAR -> "• Fire Resistance\n• Water Breathing\n• Düşme hasarı yok\n"
                    + "• Sağ tık: Wind Charge\n• Shift+sağ tık: Fire Charge";
        });
    }

    private Book buildCommandsBook() {
        List<Component> pages = new ArrayList<>();

        pages.add(Component.text(Lang.t("Komutlar (1/2)\n\n"), NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                .append(Component.text(Lang.t("/element\nElement yoksa seçim menüsü, varsa bilgi\n\n"
                                + "/element sec\nElement seçim menüsü\n\n"
                                + "/element tus\nTuş düzeni menüsü\n\n"
                                + "/element bilgi\nElementin, tuşların ve beklemelerin"),
                        NamedTextColor.BLACK).decoration(TextDecoration.BOLD, false)));

        pages.add(Component.text(Lang.t("Komutlar (2/2)\n\n"), NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                .append(Component.text(Lang.t("/avatar transfer <oyuncu>\nAvatarlığı devret (hedef 60 sn içinde /avatar kabul yazmalı)\n\n"
                                + "/guide\nBu rehber menüsü\n\n"
                                + "/nightvision\nSınırsız gece görüşü aç/kapat\n\n"
                                + "Güç kullanımı:\nEğilirken (Shift) 1-5 tuşları"),
                        NamedTextColor.BLACK).decoration(TextDecoration.BOLD, false)));

        pages.add(Component.text(Lang.t("Admin Komutları\n\n"), NamedTextColor.DARK_RED, TextDecoration.BOLD)
                .append(Component.text(Lang.t("/element admin set <oyuncu> <element>\nZorla element atar\n\n"
                                + "/element admin reset <oyuncu>\nSeçimi sıfırlar\n\n"
                                + "/element admin list\nKontenjan durumu\n\n"
                                + "/guiac <oyuncu>\nOyuncuya seçim menüsü açar"),
                        NamedTextColor.BLACK).decoration(TextDecoration.BOLD, false)));

        pages.add(Component.text(Lang.t("Ceza Komutları\n\n"), NamedTextColor.DARK_RED, TextDecoration.BOLD)
                .append(Component.text(Lang.t("/advancedban <oyuncu> <sayı> <dakika|saat|gün> <neden>\nSüreli ban (kaldır: /pardon)\n\n"
                                + "/mute <oyuncu> <sayı> <birim> <neden>\nChat susturması\n\n"
                                + "/unmute <oyuncu>\nSusturmayı kaldır"),
                        NamedTextColor.BLACK).decoration(TextDecoration.BOLD, false)));

        pages.add(Component.text(Lang.t("Duyuru Komutları\n\n"), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(Lang.t("/announcement <mesaj>\nVurgulu duyuru (yazan görünür)\n\n"
                                + "/autoannouncement <mesaj> <sayı> <dakika|saat>\nTekrarlayan imzasız duyuru\n\n"
                                + "/autoannouncement liste | sil <id>\nOtomatik duyuruları yönet"),
                        NamedTextColor.BLACK).decoration(TextDecoration.BOLD, false)));

        return Book.book(Component.text(Lang.t("Komutlar")), Component.text("ElementBending"), pages);
    }
}
