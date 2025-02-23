package de.tmxx.trading.module.i18n;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.tmxx.trading.i18n.I18n;
import de.tmxx.trading.i18n.I18nCache;
import de.tmxx.trading.i18n.I18nCacheFactory;
import de.tmxx.trading.i18n.impl.I18nCacheImpl;
import de.tmxx.trading.i18n.impl.I18nImpl;
import de.tmxx.trading.module.plugin.DataFolder;
import de.tmxx.trading.module.plugin.MainConfig;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class I18nModule extends AbstractModule {
    private static final String DEFAULT_LOCALES_DIRECTORY = "./locales";
    private static final Locale FALLBACK_LOCALE = Locale.US;

    @Override
    protected void configure() {
        // bind the factory for i18n caches
        install(new FactoryModuleBuilder().implement(I18nCache.class, I18nCacheImpl.class).build(I18nCacheFactory.class));

        bind(I18n.class).to(I18nImpl.class);
    }

    @Provides
    @LocalesDirectory
    @Singleton
    File provideLocalesDirectory(@MainConfig FileConfiguration mainConfig, @DataFolder File dataFolder) {
        String directoryPath = mainConfig.getString("locales.directory", DEFAULT_LOCALES_DIRECTORY);

        // using a directory relative to the data folder if the path starts with a '.'
        return directoryPath.startsWith(".") ? new File(dataFolder, directoryPath) : new File(directoryPath);
    }

    @Provides
    @DefaultLocale
    @Singleton
    Locale provideDefaultLocale(@MainConfig FileConfiguration mainConfig) {
        return localeFromString(mainConfig.getString("locales.default"));
    }

    @Provides
    @SupportedLocales
    @Singleton
    List<Locale> provideSupportedLocales(@MainConfig FileConfiguration mainConfig) {
        return mainConfig.getStringList("locales.supported").stream().map(this::localeFromString).toList();
    }

    private Locale localeFromString(@Nullable String value) {
        if (value == null) return FALLBACK_LOCALE;

        String[] split = value.split("_");
        return split.length == 1 ?
                Locale.of(split[0]) : split.length == 2 ?
                Locale.of(split[0], split[1]) : Locale.of(split[0], split[1], split[2]);
    }
}
