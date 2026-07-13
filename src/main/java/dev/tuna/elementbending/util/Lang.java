package dev.tuna.elementbending.util;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dil katmani. config.yml -> language: tr | en
 * Turkce metinler kaynak/anahtar olarak kalir; en secilirse birebir
 * ve sablonlu ("{}" yer tutuculu) eslesme ile Ingilizceye cevrilir.
 * Eslesme bulunamazsa Turkce metin aynen gosterilir (guvenli geri donus).
 */
public final class Lang {

    private static boolean english = false;

    private static final Map<String, String> EXACT = new HashMap<>();
    private record Template(Pattern pattern, String english) {
    }
    private static final List<Template> TEMPLATES = new ArrayList<>();

    private Lang() {
    }

    public static void init(Plugin plugin) {
        english = "en".equalsIgnoreCase(plugin.getConfig().getString("language", "tr"));
    }

    public static String t(String message) {
        if (!english || message == null) {
            return message;
        }
        String exact = EXACT.get(message);
        if (exact != null) {
            return exact;
        }
        for (Template template : TEMPLATES) {
            Matcher matcher = template.pattern().matcher(message);
            if (matcher.matches()) {
                return substitute(template.english(), matcher);
            }
        }
        return message;
    }

    private static String substitute(String englishTemplate, Matcher matcher) {
        String[] parts = englishTemplate.split("\\{\\}", -1);
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            builder.append(i <= matcher.groupCount() ? matcher.group(i) : "").append(parts[i]);
        }
        return builder.toString();
    }

    private static void e(String tr, String en) {
        EXACT.put(tr, en);
    }

    private static void p(String trTemplate, String enTemplate) {
        String[] parts = trTemplate.split("\\{\\}", -1);
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < parts.length; i++) {
            regex.append(Pattern.quote(parts[i]));
            if (i < parts.length - 1) {
                regex.append("(.*?)");
            }
        }
        regex.append("$");
        TEMPLATES.add(new Template(Pattern.compile(regex.toString(), Pattern.DOTALL), enTemplate));
    }

    static {
        // --- Genel ---
        e("Bu komut yalnızca oyuncular içindir.", "This command is for players only.");
        e("Bu komut yalnızca oyuncular içindir. Admin: /element admin", "This command is for players only. Admin: /element admin");
        e("Bu komut için yetkin yok!", "You don't have permission for this command!");
        e("Oyuncu bulunamadı (çevrimiçi olmalı).", "Player not found (must be online).");
        e("Geçersiz sayı!", "Invalid number!");
        e("Geçersiz id!", "Invalid id!");
        e("dakika", "minutes");
        e("saat", "hours");
        e("gün", "days");

        // --- Element / guc isimleri ---
        e("Hava Bükücü", "Air Bender");
        e("Su Bükücü", "Water Bender");
        e("Ateş Bükücü", "Fire Bender");
        e("Toprak Bükücü", "Earth Bender");
        e("Hava Sıçrayışı", "Air Jump");
        e("Hava Patlaması", "Air Blast");
        e("Kasırga", "Tornado");
        e("Alev Dalgası", "Flame Wave");
        e("Ateş Sıçrayışı", "Fire Dash");
        e("Ejderha Alevi", "Dragon Fire");
        e("Su İtişi", "Water Push");
        e("Buz Zinciri", "Ice Chain");
        e("Şifa Dalgası", "Healing Wave");
        e("Kaya Yumruğu", "Rock Punch");
        e("Taş Duvar", "Stone Wall");
        e("Deprem", "Earthquake");
        e("Seni havaya fırlatır.", "Launches you into the air.");
        e("Yakındaki tüm canlıları geri iter.", "Pushes back all nearby living beings.");
        e("Yakındaki tüm canlıları havaya savurur, oyuncuların ağır silahlarını devre dışı bırakır.", "Hurls all nearby living beings into the air and disables players' heavy weapons.");
        e("Önündeki tüm canlıları ateşe verir.", "Sets all living beings in front of you on fire.");
        e("Ateş/lav üzerindeyken kısa süre Speed II verir.", "Grants Speed II briefly while on fire/lava.");
        e("15 sn boyunca Ghast Fireball fırlatırsın, etrafın alev alır.", "For 15s you shoot Ghast Fireballs and ignite your surroundings.");
        e("Önündeki tüm canlıları su kuvvetiyle iter.", "Pushes all living beings in front of you with water force.");
        e("Baktığın canlıyı kısa süre dondurur.", "Briefly freezes the being you're looking at.");
        e("Seni ve yakın takım arkadaşlarını iyileştirir.", "Heals you and nearby teammates.");
        e("Sonraki yakın dövüş vuruşun ekstra hasar verir.", "Your next melee hit deals extra damage.");
        e("Önünde kısa süreli taş duvar oluşturur.", "Creates a temporary stone wall in front of you.");
        e("Yakındakilere Slowness III uygular ve onları sarsar.", "Applies Slowness III to those nearby and shakes them.");
        e("15 sn boyunca güçlenirsin, ardından kısa süre zayıflarsın.", "You're empowered for 15s, then briefly weakened.");

        // --- Guc kullanimi ---
        e("Ateş veya lav üzerinde olmalısın!", "You must be on fire or lava!");
        e("Hedef bulunamadı!", "No target found!");
        e("Duvar için yer yok!", "No room for the wall!");
        e("Yer sarsılıyor!", "The ground is shaking!");
        e("Donmuş haldeyken güç kullanamazsın!", "You can't use powers while frozen!");
        e("Silahın rüzgârla savruldu, kısa süre kullanamazsın!", "Your weapon was swept away by the wind; you can't use it briefly!");
        p("⏳ {}: {} sn", "⏳ {}: {}s");
        p("✦ {} kullanıldı", "✦ {} used");
        p("⏳ Bekle: {} sn", "⏳ Wait: {}s");
        p("Sonraki yumruğun +{} hasar verecek! ({} sn)", "Your next punch deals +{} damage! ({}s)");
        p("Ejderha Alevi aktif! {} sn boyunca boş elle sağ tık = Ghast Fireball.", "Dragon Fire active! For {}s, empty-hand right-click = Ghast Fireball.");
        p("Kasırgaya kapıldın! Ağır silahların {} sn devre dışı.", "Caught in the tornado! Your heavy weapons are disabled for {}s.");
        p("{} sana şifa verdi!", "{} healed you!");
        p("Donduruldun! ({} sn)", "You are frozen! ({}s)");
        p("{} donduruldu!", "{} was frozen!");
        p("Avatar State aktif! ({} sn)", "Avatar State active! ({}s)");
        p("Avatar State sona erdi. {} sn boyunca zayıfsın.", "Avatar State ended. You are weakened for {}s.");

        // --- Element secimi / komutlar ---
        e("Element Seçimi", "Element Selection");
        e("Tuş Ayarları", "Keybind Settings");
        e("Seçim kalıcıdır!", "Choice is permanent!");
        e("✔ Seçmek için tıkla", "✔ Click to choose");
        e("✖ DOLU", "✖ FULL");
        e("Zaten bir element seçtin. Seçimler kalıcıdır!", "You already chose an element. Choices are permanent!");
        e("Önce bir element seçmelisin: /element sec", "You must choose an element first: /element sec");
        e("Henüz bir element seçmedin: /element sec", "You haven't chosen an element yet: /element sec");
        e("Henüz bir element seçmedin! Seçim için: /element", "You haven't chosen an element yet! To choose: /element");
        e("Elementini seç! Menüyü kapatırsan tekrar açmak için: /element sec", "Choose your element! If you close the menu, reopen with: /element sec");
        e("Tuş düzeni için: /element tus", "Keybind layout: /element tus");
        e("Tuş düzenini değiştirmek için: /element tus", "To change your keybinds: /element tus");
        e("Kullanım: /element [sec | tus | bilgi]", "Usage: /element [sec | tus | bilgi]");
        e("Kullanım: /element admin <set|reset|list|maxplayerperelement> ...", "Usage: /element admin <set|reset|list|maxplayerperelement> ...");
        e("Kullanım: /element admin <set|reset|list|maxplayerperelement>", "Usage: /element admin <set|reset|list|maxplayerperelement>");
        e("Kullanım: /element admin set <oyuncu> <element>", "Usage: /element admin set <player> <element>");
        e("Kullanım: /element admin reset <oyuncu>", "Usage: /element admin reset <player>");
        e("Kullanım: /element admin maxplayerperelement <sayı>", "Usage: /element admin maxplayerperelement <number>");
        e("Sayı 1 ile 10000 arasında olmalı!", "Number must be between 1 and 10000!");
        e("Not: limit düşürülürse mevcut seçimler silinmez, yalnızca yeni seçimler engellenir.", "Note: lowering the limit doesn't remove existing choices; only new picks are blocked.");
        e("Yerde takip edilen Avatar Ruhu yok.", "No tracked Avatar Soul on the ground.");
        e("Geçersiz element! (AIR, WATER, FIRE, EARTH, AVATAR)", "Invalid element! (AIR, WATER, FIRE, EARTH, AVATAR)");
        e("Elementin sıfırlandı. Yeni seçim: /element sec", "Your element was reset. New pick: /element sec");
        p("Seçilen: {}/{}", "Chosen: {}/{}");
        p("{} kontenjanı dolu!", "{} quota is full!");
        p("{} oldun! Güçlerin: Shift + 1-2-3 tuşları.", "You are now {}! Your powers: the Shift + 1-2-3 keys.");
        p("{} oldun! Güçlerin: Shift + 1-2-3-4-5 tuşları.", "You are now {}! Your powers: the Shift + 1-2-3-4-5 keys.");
        p("Elementin: {}", "Your element: {}");
        p("Shift+{} → {} (⏳ {} sn)", "Shift+{} → {} (⏳ {}s)");
        p("Shift+{} → {}", "Shift+{} → {}");
        p("{} → {} olarak ayarlandı.", "{} was set to {}.");
        p("Elementin yönetici tarafından {} olarak ayarlandı!", "Your element was set to {} by an admin!");
        p("{} oyuncusunun elementi sıfırlandı.", "{}'s element was reset.");
        p("Element başına maksimum oyuncu sayısı {} olarak ayarlandı.", "Max players per element set to {}.");
        p("{} adet yerdeki Avatar Ruhu kaldırıldı.", "Removed {} dropped Avatar Soul(s) from the ground.");
        p("Uyarı: {} zaten element seçmiş; menü açıldı ama seçim yapamaz. Önce /element admin reset {}", "Warning: {} already has an element; the menu opened but they can't pick. First: /element admin reset {}");
        p("{} oyuncusuna seçim menüsü açıldı.", "Selection menu opened for {}.");
        e("Kullanım: /guiac <oyuncu>", "Usage: /guiac <player>");
        e("Element seçim menüsü açıldı!", "Element selection menu opened!");

        // --- Avatar ruhu / pusula / transfer ---
        e("Avatar Ruhu", "Avatar Soul");
        e("Avatar Pusulası", "Avatar Compass");
        e("Sağ tık: Avatar ol!", "Right-click: become the Avatar!");
        e("Aktifken envanterine kilitlenir.", "Locks into your inventory when active.");
        e("Avatar ölürse ruh el değiştirir!", "If the Avatar dies, the soul changes hands!");
        e("Sunucuda yalnızca 1 Avatar olabilir.", "Only 1 Avatar can exist on the server.");
        e("Yerde duran Avatar Ruhu'nu gösterir.", "Points to the Avatar Soul lying on the ground.");
        e("Ruh yerde değilse iğne boşta döner.", "If the soul isn't on the ground, the needle spins.");
        e("Zaten Avatar'sın!", "You're already the Avatar!");
        e("Sunucuda zaten bir Avatar var!", "There is already an Avatar on the server!");
        e("Avatar Ruhu envanterine kilitlendi — çıkarılamaz, ölürsen el değiştirir!", "The Avatar Soul is locked into your inventory — it can't be removed and changes hands if you die!");
        e("Güçlerin: Shift + 1-5. Tuş düzeni: /element tus", "Your powers: Shift + 1-5. Keybinds: /element tus");
        e("Avatar Ruhu yerinden çıkarılamaz!", "The Avatar Soul can't be moved!");
        e("Avatar Ruhu atılamaz!", "The Avatar Soul can't be dropped!");
        e("Avatar Ruhu'nu kaybettin. Yeni element seçebilirsin: /element sec", "You lost the Avatar Soul. You can pick a new element: /element sec");
        e("Avatar Ruhu'nu ele geçirdin! Sağ tıklayarak Avatar olabilirsin.", "You obtained the Avatar Soul! Right-click to become the Avatar.");
        e("Avatar Ruhu'nu aldın! Sağ tıklayarak Avatar olabilirsin.", "You picked up the Avatar Soul! Right-click to become the Avatar.");
        e("☠ AVATAR RUHU yere düştü! İlk alan sahibi olur! Avatar Pusulası ruhun yerini gösterir.", "☠ The AVATAR SOUL has dropped! First to grab it owns it! The Avatar Compass points to it.");
        e("Seçilemez — craft ile elde edilir!", "Not selectable — obtained by crafting!");
        e("Tarif (3x3):", "Recipe (3x3):");
        e("Tüy | Netherite | Deniz Kalbi", "Feather | Netherite | Heart of the Sea");
        e("Netherite | Ejderha Yumurtası | Netherite", "Netherite | Dragon Egg | Netherite");
        e("Blaze Tozu | Netherite | Taş", "Blaze Powder | Netherite | Stone");
        e("Avatar seçilemez! Avatar Ruhu itemini craftlayıp sağ tıklamalısın.", "Avatar can't be selected! Craft the Avatar Soul item and right-click it.");
        p("Durum: {}/{} (DOLU)", "Status: {}/{} (FULL)");
        p("Durum: {}/{} (BOŞ)", "Status: {}/{} (OPEN)");
        p("⬢ {} yeni AVATAR oldu!", "⬢ {} is the new AVATAR!");
        p("⚒ {} AVATAR RUHU'nu craftladı! Sağ tıklarsa yeni Avatar olacak!", "⚒ {} crafted the AVATAR SOUL! Right-clicking it will make them the new Avatar!");
        p("⬢ AVATAR {} öldü!", "⬢ AVATAR {} has died!");
        p("⚔ {}, AVATAR {}'i öldürerek Avatar Ruhu'nu ele geçirdi!", "⚔ {} killed AVATAR {} and seized the Avatar Soul!");
        p("✦ {} yerdeki AVATAR RUHU'nu kaptı! Ruh artık onun!", "✦ {} grabbed the AVATAR SOUL from the ground! It's theirs now!");
        e("Kullanım: /avatar transfer <oyuncu> | /avatar kabul", "Usage: /avatar transfer <player> | /avatar kabul");
        e("Kullanım: /avatar transfer <oyuncu>", "Usage: /avatar transfer <player>");
        e("Bu komutu yalnızca mevcut Avatar kullanabilir!", "Only the current Avatar can use this command!");
        e("Avatarlığı kendine devredemezsin!", "You can't transfer the Avatar title to yourself!");
        e("Kabul etmek için 60 saniye içinde şunu yaz: /avatar kabul", "To accept, type within 60 seconds: /avatar kabul");
        e("Dikkat: kabul edersen mevcut elementini kaybedersin!", "Warning: accepting will remove your current element!");
        e("Sana yapılmış bir devir teklifi yok.", "You have no pending transfer offer.");
        e("Devir teklifinin süresi dolmuş (60 sn).", "The transfer offer has expired (60s).");
        e("Devreden oyuncu artık çevrimiçi değil; teklif geçersiz.", "The offering player is no longer online; the offer is void.");
        e("Avatarlığı devrettin. Yeni element seçebilirsin: /element sec", "You transferred the Avatar title. You can pick a new element: /element sec");
        e("Artık AVATAR'sın! Ruh envanterine kilitlendi. Güçlerin: Shift + 1-5", "You are now the AVATAR! The soul is locked into your inventory. Powers: Shift + 1-5");
        p("{} oyuncusuna devir teklifi gönderildi. 60 saniye içinde kabul etmezse geçersiz olur.", "Transfer offer sent to {}. It becomes void if not accepted within 60 seconds.");
        p("{} AVATARLIĞINI sana devretmek istiyor!", "{} wants to transfer the AVATAR title to you!");
        p("{} artık Avatar değil; teklif geçersiz.", "{} is no longer the Avatar; the offer is void.");
        p("⬢ {}, AVATARLIĞINI {} oyuncusuna devretti!", "⬢ {} transferred the AVATAR title to {}!");

        // --- Rehber ---
        e("Rehber", "Guide");
        e("Komutlar", "Commands");
        e("Tüm komutları görmek için tıkla", "Click to see all commands");
        e("Pasifler ve güçler için tıkla", "Click for passives and powers");
        e("Pasifler:\n", "Passives:\n");
        e("ElementBending", "ElementBending");
        p("{} Rehberi", "{} Guide");
        p("Bekleme: {} sn", "Cooldown: {}s");
        e("• Speed I\n• Düşme hasarı yok\n• Boş elle sağ tık: Wind Charge (2 sn)", "• Speed I\n• No fall damage\n• Empty-hand right-click: Wind Charge (2s)");
        e("• Water Breathing\n• Dolphin's Grace\n• Yağmurda Regeneration I", "• Water Breathing\n• Dolphin's Grace\n• Regeneration I in rain");
        e("• Fire Resistance\n• Boş elle sağ tık: Fire Charge (2 sn)\n• Yumruklar 3 sn yakar", "• Fire Resistance\n• Empty-hand right-click: Fire Charge (2s)\n• Punches ignite for 3s");
        e("• Resistance I\n• %50 geri tepme azaltma\n• Taş üzerinde Haste I", "• Resistance I\n• 50% knockback reduction\n• Haste I on stone");
        e("• Fire Resistance\n• Water Breathing\n• Düşme hasarı yok\n• Sağ tık: Wind Charge\n• Shift+sağ tık: Fire Charge", "• Fire Resistance\n• Water Breathing\n• No fall damage\n• Right-click: Wind Charge\n• Shift+right-click: Fire Charge");
        e("Avatar Ruhu\n\n", "Avatar Soul\n\n");
        e("Avatar seçilemez; Avatar Ruhu craftlanıp sağ tıklanarak olunur. Ruh envanterine kilitlenir. Avatar ölürse: katil oyuncuysa ruhu o alır, değilse ruh yere düşer ve ilk alan sahibi olur. Avatar Pusulası yerdeki ruhu gösterir.", "The Avatar can't be selected; craft the Avatar Soul and right-click it. The soul locks into your inventory. If the Avatar dies: a player killer takes the soul, otherwise it drops and the first to grab it owns it. The Avatar Compass points to the dropped soul.");
        e("Komutlar (1/2)\n\n", "Commands (1/2)\n\n");
        e("Komutlar (2/2)\n\n", "Commands (2/2)\n\n");
        e("Admin Komutları\n\n", "Admin Commands\n\n");
        e("Ceza Komutları\n\n", "Punishment Commands\n\n");
        e("Duyuru Komutları\n\n", "Announcement Commands\n\n");
        e("/element\nElement yoksa seçim menüsü, varsa bilgi\n\n/element sec\nElement seçim menüsü\n\n/element tus\nTuş düzeni menüsü\n\n/element bilgi\nElementin, tuşların ve beklemelerin", "/element\nSelection menu if you have no element, info otherwise\n\n/element sec\nElement selection menu\n\n/element tus\nKeybind menu\n\n/element bilgi\nYour element, keys and cooldowns");
        e("/avatar transfer <oyuncu>\nAvatarlığı devret (hedef 60 sn içinde /avatar kabul yazmalı)\n\n/guide\nBu rehber menüsü\n\n/nightvision\nSınırsız gece görüşü aç/kapat\n\nGüç kullanımı:\nEğilirken (Shift) 1-5 tuşları", "/avatar transfer <player>\nTransfer the Avatar title (target must type /avatar kabul within 60s)\n\n/guide\nThis guide menu\n\n/nightvision\nToggle unlimited night vision\n\nUsing powers:\nPress keys 1-5 while sneaking (Shift)");
        e("/element admin set <oyuncu> <element>\nZorla element atar\n\n/element admin reset <oyuncu>\nSeçimi sıfırlar\n\n/element admin list\nKontenjan durumu\n\n/guiac <oyuncu>\nOyuncuya seçim menüsü açar", "/element admin set <player> <element>\nForce-sets an element\n\n/element admin reset <player>\nResets the choice\n\n/element admin list\nQuota status\n\n/guiac <player>\nOpens the selection menu for a player");
        e("/advancedban <oyuncu> <sayı> <dakika|saat|gün> <neden>\nSüreli ban (kaldır: /pardon)\n\n/mute <oyuncu> <sayı> <birim> <neden>\nChat susturması\n\n/unmute <oyuncu>\nSusturmayı kaldır", "/advancedban <player> <number> <dakika|saat|gün> <reason>\nTimed ban (undo: /pardon)\n\n/mute <player> <number> <unit> <reason>\nChat mute\n\n/unmute <player>\nRemove a mute");
        e("/announcement <mesaj>\nVurgulu duyuru (yazan görünür)\n\n/autoannouncement <mesaj> <sayı> <dakika|saat>\nTekrarlayan imzasız duyuru\n\n/autoannouncement liste | sil <id>\nOtomatik duyuruları yönet", "/announcement <message>\nHighlighted announcement (author shown)\n\n/autoannouncement <message> <number> <dakika|saat>\nRepeating unsigned announcement\n\n/autoannouncement liste | sil <id>\nManage auto announcements");

        // --- Duyuru / ceza / nv ---
        e("📢 DUYURU » ", "📢 ANNOUNCEMENT » ");
        e("Kullanım: /announcement <mesaj>", "Usage: /announcement <message>");
        e("Kullanım: /autoannouncement <mesaj> <sayı> <dakika|saat>", "Usage: /autoannouncement <message> <number> <dakika|saat>");
        e("Diğer: /autoannouncement liste | /autoannouncement sil <id>", "Other: /autoannouncement liste | /autoannouncement sil <id>");
        e("Kayıtlı otomatik duyuru yok.", "No auto announcements saved.");
        e("Bu id ile bir duyuru bulunamadı. Liste: /autoannouncement liste", "No announcement with that id. List: /autoannouncement liste");
        e("Geçersiz süre! En az 1 dakika olmalı. Örnek: /autoannouncement Discord'a katılın! 2 saat", "Invalid duration! Minimum is 1 minute. Example: /autoannouncement Join our Discord! 2 saat");
        p("#{} [{} arayla] {}", "#{} [every {}] {}");
        p("#{} numaralı otomatik duyuru silindi.", "Auto announcement #{} deleted.");
        p("#{} numaralı otomatik duyuru eklendi ({} {} arayla). Silmek için: /autoannouncement sil {}", "Auto announcement #{} added (every {} {}). To delete: /autoannouncement sil {}");
        e("Kullanım: /advancedban <oyuncu> <sayı> <dakika|saat|gün> <neden>", "Usage: /advancedban <player> <number> <dakika|saat|gün> <reason>");
        e("Geçersiz süre! Örnek: /advancedban Oyuncu 3 gün Hacking", "Invalid duration! Example: /advancedban Player 3 gün Hacking");
        e("Bu oyuncunun da ban yetkisi var; onu banlayamazsın!", "That player also has ban permission; you can't ban them!");
        e("Uyarı: bu isimde bir oyuncu sunucuya hiç girmemiş; isim yanlışsa ban isabet etmez.", "Warning: no player with this name has ever joined; if it's misspelled the ban won't apply.");
        e("Sunucudan yasaklandın!\n\n", "You have been banned from this server!\n\n");
        p("Süre: {}\n", "Duration: {}\n");
        p("Sebep: {}", "Reason: {}");
        p("⛔ {}, {} tarafından {} yasaklandı! Sebep: {}", "⛔ {} was banned by {} for {}! Reason: {}");
        e("Kullanım: /mute <oyuncu> <sayı> <dakika|saat|gün> <neden>", "Usage: /mute <player> <number> <dakika|saat|gün> <reason>");
        e("Geçersiz süre! Örnek: /mute Oyuncu 30 dakika Spam", "Invalid duration! Example: /mute Player 30 dakika Spam");
        e("Kullanım: /unmute <oyuncu>", "Usage: /unmute <player>");
        e("Bu oyuncu zaten susturulmamış.", "That player isn't muted.");
        e("Susturman kaldırıldı, tekrar konuşabilirsin.", "Your mute was removed; you can talk again.");
        p("Susturuldun! Süre: {} — Sebep: {}", "You are muted! Duration: {} — Reason: {}");
        p("Susturuldun! Kalan: {} — Sebep: {}", "You are muted! Remaining: {} — Reason: {}");
        p("🔇 {}, {} tarafından {} susturuldu! Sebep: {}", "🔇 {} was muted by {} for {}! Reason: {}");
        p("{} oyuncusunun susturması kaldırıldı.", "{}'s mute was removed.");
        e("Gece görüşü kapatıldı.", "Night vision disabled.");
        e("Sınırsız Gece Görüşü II açıldı! Kapatmak için tekrar yaz.", "Unlimited Night Vision II enabled! Type again to disable.");
    }
}
