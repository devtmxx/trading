package de.tmxx.trading.module.trade;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradeFactory;
import de.tmxx.trading.trade.inventory.TradeInventory;
import de.tmxx.trading.trade.impl.TradeImpl;
import de.tmxx.trading.trade.inventory.TradeInventoryBuilder;
import de.tmxx.trading.trade.inventory.TradeInventoryHandler;
import de.tmxx.trading.trade.inventory.impl.TradeInventoryBuilderImpl;
import de.tmxx.trading.trade.inventory.impl.TradeInventoryHandlerImpl;
import de.tmxx.trading.trade.inventory.impl.TradeInventoryImpl;

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
                .implement(TradeInventoryBuilder.class, TradeInventoryBuilderImpl.class)
                .implement(TradeInventoryHandler.class, TradeInventoryHandlerImpl.class)
                .build(TradeFactory.class));
    }
}
