package de.tmxx.trading.trade;

import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.inventory.TradeInventory;
import de.tmxx.trading.trade.inventory.TradeInventoryBuilder;
import de.tmxx.trading.trade.inventory.TradeInventoryHandler;
import de.tmxx.trading.user.User;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeFactory {
    Trade create(@Assisted("initiator") User initiator, @Assisted("partner") User partner);
    TradeInventory createInventory(Trade trade, User user);
    TradeInventoryBuilder createInventoryBuilder(Trade trade, User user);
    TradeInventoryHandler createInventoryHandler(Trade trade, User user);
}
