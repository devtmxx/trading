package de.tmxx.trading.i18n;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface I18nCache {
    String PREFIX_KEY = "prefix";
    String REPLACE_PREFIX = "%prefix%";

    String get(String key, Object... args);
}
