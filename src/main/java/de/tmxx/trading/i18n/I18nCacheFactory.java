package de.tmxx.trading.i18n;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface I18nCacheFactory {
    I18nCache create(FileConfiguration config);
}
