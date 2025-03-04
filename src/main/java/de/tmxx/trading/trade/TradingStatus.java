package de.tmxx.trading.trade;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradingStatus {
    boolean isEnabled();
    void toggle();
}
