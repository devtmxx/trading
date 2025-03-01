package de.tmxx.trading.user.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.i18n.I18n;
import de.tmxx.trading.trade.TradeInventory;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class UserImpl implements User {
    private static final long REQUEST_TIMEOUT = TimeUnit.MINUTES.toMillis(2);

    private final I18n i18n;
    private final Player player;

    private final Map<UUID, Long> requests = new HashMap<>();

    private TradeInventory currentInventory = null;
    private ItemStack[] currentTradeContents = new ItemStack[12];
    private int currentBid = 0;
    private TradingState currentTradingState = TradingState.TRADING;

    @Inject
    UserImpl(I18n i18n, @Assisted Player player) {
        this.i18n = i18n;
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getIp() {
        InetSocketAddress address = player.getAddress();
        if (address == null) return "";

        return address.getAddress().getHostAddress();
    }

    @Override
    public void sendMessage(String key, Object... args) {
        player.sendMessage(i18n.translate(player.locale(), key, args));
    }

    @Override
    public Component translate(String key, Object... args) {
        return i18n.translate(player.locale(), key, args);
    }

    @Override
    public List<Component> translateLore(String key, Object... args) {
        return i18n.translateLore(player.locale(), key, args);
    }

    @Override
    public void addRequest(User requestedUser) {
        requests.put(requestedUser.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public boolean hasRequest(User requestedUser) {
        long deltaTimeMillis = System.currentTimeMillis() - requests.getOrDefault(requestedUser.getUniqueId(), 0L);
        return deltaTimeMillis < REQUEST_TIMEOUT;
    }

    @Override
    public void invalidateRequest(User requestedUser) {
        requests.remove(requestedUser.getUniqueId());
    }

    @Override
    public void setInventory(TradeInventory inventory) {
        currentInventory = inventory;
    }

    @Override
    public TradeInventory getInventory() {
        return currentInventory;
    }

    @Override
    public void setTradeContents(ItemStack[] contents) {
        currentTradeContents = contents;
    }

    @Override
    public ItemStack[] getTradeContents() {
        return currentTradeContents;
    }

    @Override
    public void resetCurrentBid() {
        currentBid = 0;
    }

    @Override
    public void changeCurrentBid(int change) {
        currentBid += change;
    }

    @Override
    public int getCurrentBid() {
        return currentBid;
    }

    @Override
    public void setTradingState(TradingState state) {
        currentTradingState = state;
    }

    @Override
    public TradingState getTradingState() {
        return currentTradingState;
    }
}
