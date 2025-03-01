package de.tmxx.trading.module.trade;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradeFactory;
import de.tmxx.trading.trade.TradeInventory;
import de.tmxx.trading.trade.impl.TradeImpl;
import de.tmxx.trading.trade.impl.TradeInventoryImpl;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(Trade.class, TradeImpl.class)
                .implement(TradeInventory.class, TradeInventoryImpl.class)
                .build(TradeFactory.class));
    }
}
