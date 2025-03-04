package de.tmxx.trading.trade.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tmxx.trading.trade.TradingStatus;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class TradingStatusImpl implements TradingStatus {
    private final JavaPlugin plugin;
    private boolean enabled = false;

    @Inject
    TradingStatusImpl(JavaPlugin plugin) {
        this.plugin = plugin;

        load();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void toggle() {
        enabled = !enabled;

        save();
    }

    private void load() {
        enabled = plugin.getConfig().getBoolean("enabled", true);
    }

    private void save() {
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }
}
