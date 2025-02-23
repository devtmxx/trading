package de.tmxx.trading.i18n.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tmxx.trading.i18n.I18n;
import de.tmxx.trading.i18n.I18nCache;
import de.tmxx.trading.i18n.I18nCacheFactory;
import de.tmxx.trading.module.i18n.DefaultLocale;
import de.tmxx.trading.module.i18n.LocalesDirectory;
import de.tmxx.trading.module.i18n.SupportedLocales;
import de.tmxx.trading.module.plugin.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class I18nImpl implements I18n {
    private static final String LORE_SPLIT = "%n";
    private static final Set<Locale> DEFAULT_LOCALES = Set.of(Locale.US, Locale.GERMANY);
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(I18nImpl.class);

    private final Logger logger;
    private final File directory;
    private final Locale defaultLocale;
    private final List<Locale> supportedLocales;
    private final Map<Locale, I18nCache> cache = new HashMap<>();

    @Inject
    I18nImpl(
            JavaPlugin plugin,
            I18nCacheFactory factory,
            @PluginLogger Logger logger,
            @LocalesDirectory File directory,
            @DefaultLocale Locale defaultLocale,
            @SupportedLocales List<Locale> supportedLocales
    ) {
        this.logger = logger;
        this.directory = directory;
        this.defaultLocale = defaultLocale;
        this.supportedLocales = supportedLocales;

        saveDefaultLocales(plugin);
        loadCache(factory);
    }

    @Override
    public Component translate(Locale locale, String key, Object... args) {
        return translate(MiniMessage.miniMessage(), locale, key, args);
    }

    @Override
    public Component translate(MiniMessage miniMessage, Locale locale, String key, Object... args) {
        return miniMessage.deserialize(translateRaw(locale, key, args));
    }

    @Override
    public List<Component> translateLore(Locale locale, String key, Object... args) {
        return translateLore(MiniMessage.miniMessage(), locale, key, args);
    }

    @Override
    public List<Component> translateLore(MiniMessage miniMessage, Locale locale, String key, Object... args) {
        // lists are used for item lores and as item names/lores will - for whatever reason - be automatically formatted
        // italic, we explicitly remove any italic decorations for lists.
        return Arrays.stream(translateRaw(locale, key, args).split(LORE_SPLIT))
                .map(value -> miniMessage.deserialize(value).decoration(TextDecoration.ITALIC, false))
                .toList();
    }

    @Override
    public String translateRaw(String key, Object... args) {
        return translateRaw(defaultLocale, key, args);
    }

    @Override
    public String translateRaw(Locale locale, String key, Object... args) {
        if (!locale.equals(defaultLocale) && !supportedLocales.contains(locale)) return translateRaw(defaultLocale, key, args);

        I18nCache cache = this.cache.get(locale);
        if (cache == null) return TRANSLATION_NOT_AVAILABLE;

        return cache.get(key, args);
    }

    private void loadCache(I18nCacheFactory factory) {
        supportedLocales.forEach(locale -> {
            File localeFile = new File(directory, locale.toString() + ".yml");
            if (!localeFile.exists()) {
                logger.warning("Tried to load supported locale " + locale + " but could not find locale file at " + localeFile.getAbsolutePath());
                return;
            }

            cache.put(locale, factory.create(YamlConfiguration.loadConfiguration(localeFile)));
        });
    }

    private void saveDefaultLocales(Plugin plugin) {
        DEFAULT_LOCALES.forEach(locale -> {
            File file = new File(directory, locale.toString() + ".yml");
            // Skipping saveResource if the file exists because this will otherwise print an ugly warning into the console
            if (file.exists()) return;

            plugin.saveResource("locales/%s.yml".formatted(locale.toString()), false);
        });
    }
}
