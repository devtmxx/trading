package de.tmxx.trading.trade;

import de.tmxx.trading.user.User;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface Trade {
    void start();
    User getPartner(User user);

    void resetCountdown();
    void decrementCountdown();
    int getCountdown();
}
