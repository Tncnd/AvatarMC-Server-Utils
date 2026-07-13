package dev.tuna.elementbending.util;

import java.util.Locale;

/**
 * Sure ayristirma: sayi + birim (dakika/saat/gun, TR ve EN).
 */
public final class DurationParser {

    private DurationParser() {
    }

    /**
     * @return sure (milisaniye) veya gecersizse null
     */
    public static Long parseMillis(String amountArg, String unitArg) {
        long amount;
        try {
            amount = Long.parseLong(amountArg);
        } catch (NumberFormatException ex) {
            return null;
        }
        if (amount <= 0 || amount > 100_000) {
            return null;
        }
        Long unitMillis = unitMillis(unitArg);
        return unitMillis == null ? null : amount * unitMillis;
    }

    private static Long unitMillis(String unit) {
        return switch (unit.toLowerCase(Locale.ROOT)) {
            case "dakika", "dk", "minute", "minutes", "min", "m" -> 60_000L;
            case "saat", "sa", "hour", "hours", "h" -> 3_600_000L;
            case "gün", "gun", "day", "days", "d" -> 86_400_000L;
            default -> null;
        };
    }

    /** Yayin mesajlari icin dil ayarina uygun birim adi. */
    public static String unitName(String unit) {
        return Lang.t(switch (unit.toLowerCase(Locale.ROOT)) {
            case "dakika", "dk", "minute", "minutes", "min", "m" -> "dakika";
            case "saat", "sa", "hour", "hours", "h" -> "saat";
            case "gün", "gun", "day", "days", "d" -> "gün";
            default -> unit;
        });
    }

    /** Kalan sureyi okunur formata cevirir (or. "2 gün 5 saat"). */
    public static String formatRemaining(long millis) {
        long totalMinutes = Math.max(1, millis / 60_000L);
        long days = totalMinutes / 1440;
        long hours = (totalMinutes % 1440) / 60;
        long minutes = totalMinutes % 60;
        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append(" ").append(Lang.t("gün")).append(" ");
        }
        if (hours > 0) {
            builder.append(hours).append(" ").append(Lang.t("saat")).append(" ");
        }
        if (minutes > 0 || builder.isEmpty()) {
            builder.append(minutes).append(" ").append(Lang.t("dakika"));
        }
        return builder.toString().trim();
    }
}
