package de.tmxx.trading.i18n.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.i18n.I18n;
import de.tmxx.trading.i18n.I18nCache;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class I18nCacheImpl implements I18nCache {
    private final Map<String, String> messages = new HashMap<>();

    @Inject
    I18nCacheImpl(@Assisted FileConfiguration config) {
        config.getKeys(true).forEach(key -> messages.put(key, config.getString(key)));

        String prefix = config.getString(PREFIX_KEY);
        if (prefix == null) return;

        replacePrefix(prefix);
    }

    @Override
    public String get(String key, Object... args) {
        if (!messages.containsKey(key)) return I18n.TRANSLATION_NOT_AVAILABLE;
        return MessageFormat.format(messages.get(key), args);
    }

    private void replacePrefix(String prefix) {
        messages.replaceAll((key, value) -> value.replaceAll(REPLACE_PREFIX, prefix));
    }
}
