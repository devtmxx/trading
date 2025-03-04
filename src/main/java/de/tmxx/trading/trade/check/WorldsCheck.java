package de.tmxx.trading.trade.check;

import com.google.inject.Inject;
import de.tmxx.trading.module.plugin.MainConfig;
import de.tmxx.trading.user.User;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class WorldsCheck implements TradeLicenseCheck {
    private final boolean enabled;

    @Inject
    WorldsCheck(@MainConfig FileConfiguration config) {
        enabled = config.getBoolean("check.worlds.enabled", true);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean check(User initiator, User partner) {
        boolean passed = initiator.getPlayer().getWorld().equals(partner.getPlayer().getWorld());

        if (!passed) {
            initiator.sendMessage("check.worlds.failed", partner.getName());
        }

        return passed;
    }
}
