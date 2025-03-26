package de.tmxx.trading;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tmxx.trading.helper.CommandHelper;
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
    private final CommandHelper commandHelper;

    @Inject
    TradingCore(ListenerHelper listenerHelper, CommandHelper commandHelper) {
        this.listenerHelper = listenerHelper;
        this.commandHelper = commandHelper;
    }

    public void enable() {
        listenerHelper.registerListeners();
        commandHelper.registerCommands();
    }
}
