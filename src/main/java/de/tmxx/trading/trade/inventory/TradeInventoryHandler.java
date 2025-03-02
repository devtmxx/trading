package de.tmxx.trading.trade.inventory;

import org.bukkit.event.inventory.ClickType;

/**
 * Project: trading
 * 02.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TradeInventoryHandler {
    void updateState();
    void cancelTrade();
    void execute(int slot, ClickType type);
}
