package de.tmxx.trading.trade.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.user.User;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeImpl implements Trade {
    private static final int START_COUNTDOWN = 5;

    private final User initiator;
    private final User partner;

    private int countdown = START_COUNTDOWN;

    @Inject
    TradeImpl(@Assisted("initiator") User initiator, @Assisted("partner") User partner) {
        this.initiator = initiator;
        this.partner = partner;
    }

    @Override
    public void start() {
        initiator.getInventory().open();
        partner.getInventory().open();
    }

    @Override
    public User getPartner(User user) {
        return user.equals(initiator) ? partner : initiator;
    }

    @Override
    public void resetCountdown() {
        countdown = START_COUNTDOWN;
    }

    @Override
    public void decrementCountdown() {
        countdown--;
    }

    @Override
    public int getCountdown() {
        return countdown;
    }
}
