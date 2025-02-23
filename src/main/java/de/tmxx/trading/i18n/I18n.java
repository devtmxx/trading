package de.tmxx.trading.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Locale;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface I18n {
    String TRANSLATION_NOT_AVAILABLE = "N/A";

    Component translate(Locale locale, String key, Object... args);
    Component translate(MiniMessage miniMessage, Locale locale, String key, Object... args);
    List<Component> translateLore(Locale locale, String key, Object... args);
    List<Component> translateLore(MiniMessage miniMessage, Locale locale, String key, Object... args);
    String translateRaw(String key, Object... args);
    String translateRaw(Locale locale, String key, Object... args);
}
