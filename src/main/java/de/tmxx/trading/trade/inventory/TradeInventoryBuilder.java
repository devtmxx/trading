package de.tmxx.trading.trade.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Project: trading
 * 02.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeInventoryBuilder {
    Inventory newInventory(InventoryHolder holder);
    void updateContent();
}
