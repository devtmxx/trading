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
public class DistanceCheck implements TradeLicenseCheck {
    private final boolean enabled;
    private final double distance;

    @Inject
    DistanceCheck(@MainConfig FileConfiguration config) {
        enabled = config.getBoolean("check.distance.enabled", true);
        distance = Math.pow(config.getInt("check.distance.distance", 100), 2);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean check(User initiator, User partner) {
        boolean worldsPassed = initiator.getPlayer().getWorld().equals(partner.getPlayer().getWorld());
        boolean passed = worldsPassed && initiator.getPlayer().getLocation().distanceSquared(partner.getPlayer().getLocation()) <= distance;

        if (!passed) {
            initiator.sendMessage("check.distance.failed", partner.getDisplayName());
        }

        return passed;
    }
}
