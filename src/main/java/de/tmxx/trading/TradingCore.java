package de.tmxx.trading;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tmxx.trading.helper.ListenerHelper;

/**
 * Project: trading
 * 23.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class TradingCore {
    private final ListenerHelper listenerHelper;

    @Inject
    TradingCore(ListenerHelper listenerHelper) {
        this.listenerHelper = listenerHelper;
    }

    public void enable() {
        listenerHelper.register();
    }

    public void disable() {

    }
}
