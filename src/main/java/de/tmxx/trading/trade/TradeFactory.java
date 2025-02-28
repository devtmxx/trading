package de.tmxx.trading.trade;

import de.tmxx.trading.user.User;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeFactory {
    Trade create(User initiator, User partner);
}
