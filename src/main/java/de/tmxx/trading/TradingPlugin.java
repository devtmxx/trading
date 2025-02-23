package de.tmxx.trading;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tmxx.trading.module.i18n.I18nModule;
import de.tmxx.trading.module.plugin.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradingPlugin extends JavaPlugin {
    public static final int CONFIG_VERSION = 1;
    private TradingCore core = null;

    @Override
    public void onEnable() {
        Injector injector = Guice.createInjector(
                new PluginModule(this),
                new I18nModule()
        );

        core = injector.getInstance(TradingCore.class);
        core.enable();
    }

    @Override
    public void onDisable() {
        // do nothing if the plugin has not been enabled correctly
        if (core == null) return;

        core.disable();
    }
}
