package de.tmxx.trading.trade;

import de.tmxx.trading.user.User;
import org.jetbrains.annotations.Nullable;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface Trade {
    void start();
    void cancel(@Nullable User cancelledBy);
    User getPartner(User user);

    void resetCountdown();
    int getCountdown();

    void checkTradingStates();
}
