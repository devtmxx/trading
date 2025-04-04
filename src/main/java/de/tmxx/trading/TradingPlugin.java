package de.tmxx.trading;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tmxx.trading.module.helper.HelperModule;
import de.tmxx.trading.module.i18n.I18nModule;
import de.tmxx.trading.module.plugin.PluginModule;
import de.tmxx.trading.module.trade.TradeModule;
import de.tmxx.trading.module.user.UserModule;
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
    private static Injector unsafe;

    @Override
    public void onEnable() {
        unsafe = Guice.createInjector(
                new PluginModule(this),
                new I18nModule(),
                new UserModule(),
                new TradeModule(),
                new HelperModule()
        );

        TradingCore core = unsafe.getInstance(TradingCore.class);
        core.enable();
    }

    public static Injector unsafe() {
        return unsafe;
    }
}
