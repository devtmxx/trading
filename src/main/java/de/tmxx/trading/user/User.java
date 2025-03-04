package de.tmxx.trading.user;

import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.inventory.TradeInventory;
import de.tmxx.trading.trade.TradingState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface User {
    Player getPlayer();
    String getName();
    String getDisplayName();
    UUID getUniqueId();
    String getIp();
    void sendMessage(String key, Object... args);
    Component translate(String key, Object... args);
    List<Component> translateLore(String key, Object... args);

    void addRequest(User requestedUser);
    boolean hasRequest(User requestedUser);
    void invalidateRequest(User requestedUser);

    void setTrade(Trade trade);
    Trade getTrade();

    void setInventory(TradeInventory inventory);
    TradeInventory getInventory();

    void setTradeContents(ItemStack[] contents);
    void giveItems(User user);
    ItemStack[] getTradeContents();

    void resetCurrentBid();
    void changeCurrentBid(int change);
    int getCurrentBid();

    boolean isOfferValid();

    void setTradingState(TradingState state);
    TradingState getTradingState();
}
