package de.tmxx.trading.trade;

import de.tmxx.trading.user.User;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeLicenseExaminer {
    boolean check(User initiator, User partner);
}
