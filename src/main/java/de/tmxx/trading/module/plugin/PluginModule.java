package de.tmxx.trading.module.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

import static de.tmxx.trading.TradingPlugin.CONFIG_VERSION;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class PluginModule extends AbstractModule {
    private final JavaPlugin plugin;

    public PluginModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        bind(Logger.class).annotatedWith(PluginLogger.class).toInstance(plugin.getLogger());
        bind(File.class).annotatedWith(DataFolder.class).toInstance(plugin.getDataFolder());
    }

    @Provides
    @MainConfig
    @Singleton
    FileConfiguration provideMainConfig() {
        createDefaultConfigFileIfNotExists();
        updateConfigIfVersionIsOutdated();
        return plugin.getConfig();
    }

    private void createDefaultConfigFileIfNotExists() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (configFile.exists()) return;

        plugin.saveResource("config.yml", false);
    }

    private void updateConfigIfVersionIsOutdated() {
        // do nothing if the config version is up to date
        if (plugin.getConfig().getInt("version") >= CONFIG_VERSION) return;

        // caching the old config values
        FileConfiguration config = plugin.getConfig();

        // replace this old config with the new one from the plugins resources
        plugin.saveResource("config.yml", true);
        plugin.reloadConfig();

        // after replacing the old config, we can now fill the new config with the old values. doing so will lead to a
        // config containing both the old values and the new default values as well as all comments inside the config.
        FileConfiguration newConfig = plugin.getConfig();
        config.getKeys(true).forEach(key -> newConfig.set(key, config.get(key)));

        // set the new config version and save the config to the file
        newConfig.set("version", CONFIG_VERSION);
        plugin.saveConfig();
    }
}
