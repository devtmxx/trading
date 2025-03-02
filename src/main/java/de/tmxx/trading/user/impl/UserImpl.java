package de.tmxx.trading.user.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.i18n.I18n;
import de.tmxx.trading.trade.inventory.TradeInventory;
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
    public void returnItems() {
        for (int i = 0; i < currentTradeContents.length; i++) {
            ItemStack itemStack = currentTradeContents[i];

            if (itemStack == null || itemStack.isEmpty()) continue;

            // add items to the players inventory and drop all items which didn't fit
            player.getInventory().addItem(itemStack)
                    .values()
                    .forEach(toDrop -> player.getWorld().dropItem(player.getLocation(), toDrop));

            currentTradeContents[i] = null;
        }
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
