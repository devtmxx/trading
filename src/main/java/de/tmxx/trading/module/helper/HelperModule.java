package de.tmxx.trading.module.helper;

import com.google.inject.AbstractModule;
import de.tmxx.trading.helper.ListenerHelper;
import de.tmxx.trading.helper.impl.ListenerHelperImpl;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class HelperModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ListenerHelper.class).to(ListenerHelperImpl.class);
    }
}
