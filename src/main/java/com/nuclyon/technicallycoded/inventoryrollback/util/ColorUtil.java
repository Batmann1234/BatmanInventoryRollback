package com.nuclyon.technicallycoded.inventoryrollback.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad de colores: soporta codigos legacy (&a, &l...) y colores hex
 * en los formatos &#RRGGBB y <#RRGGBB>.
 */
public final class ColorUtil {

    private static final Pattern HEX_AMP = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_TAG = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private ColorUtil() {}

    public static String colorize(String text) {
        if (text == null) return null;

        text = replaceHex(HEX_AMP, text);
        text = replaceHex(HEX_TAG, text);

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String replaceHex(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append('\u00A7').append(c);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /** Quita todos los colores (incluido hex) para comparar textos. */
    public static String strip(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }

}
