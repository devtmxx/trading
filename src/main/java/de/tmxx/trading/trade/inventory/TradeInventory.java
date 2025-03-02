package de.tmxx.trading.trade.inventory;

import org.bukkit.inventory.Inventory;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeInventory {
    void open();
    Inventory getInventory();
    void update();
}
