package de.tmxx.trading.module.helper;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import de.tmxx.trading.helper.CommandHelper;
import de.tmxx.trading.helper.ListenerHelper;
import de.tmxx.trading.helper.impl.CommandHelperImpl;
import de.tmxx.trading.helper.impl.ListenerHelperImpl;
import de.tmxx.trading.module.plugin.PluginLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class HelperModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ListenerHelper.class).to(ListenerHelperImpl.class);
        bind(CommandHelper.class).to(CommandHelperImpl.class);
    }

    @Provides
    @Singleton
    Economy provideEconomy(@PluginLogger Logger logger) {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            logger.severe("Could not find Vault economy service provider");
            return null;
        }

        return economyProvider.getProvider();
    }
}
