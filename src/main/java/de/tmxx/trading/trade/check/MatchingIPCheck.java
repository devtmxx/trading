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
public class MatchingIPCheck implements TradeLicenseCheck {
    private final boolean enabled;

    @Inject
    MatchingIPCheck(@MainConfig FileConfiguration config) {
        enabled = config.getBoolean("check.matching-ip.enabled", true);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean check(User initiator, User partner) {
        boolean passed = !initiator.getIp().equals(partner.getIp());

        if (!passed) {
            initiator.sendMessage("check.matching-ip.failed");
        }

        return passed;
    }
}
