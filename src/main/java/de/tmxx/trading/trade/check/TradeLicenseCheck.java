package de.tmxx.trading.trade.check;

import de.tmxx.trading.user.User;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeLicenseCheck {
    boolean isEnabled();
    boolean check(User initiator, User partner);
}
