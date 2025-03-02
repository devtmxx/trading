package de.tmxx.trading.trade.inventory.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.trade.inventory.TradeInventoryHandler;
import de.tmxx.trading.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Project: trading
 * 02.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeInventoryHandlerImpl implements TradeInventoryHandler {
    private static final Map<Integer, Integer> OWN_SLOT_TO_ITEM_MAP = new HashMap<>();
    private static final Map<Integer, Integer> OWN_ITEM_TO_SLOT_MAP = new HashMap<>();

    private final JavaPlugin plugin;
    private final Economy economy;
    private final Trade trade;
    private final User user;

    @Inject
    TradeInventoryHandlerImpl(JavaPlugin plugin, Economy economy, @Assisted Trade trade, @Assisted User user) {
        this.plugin = plugin;
        this.economy = economy;
        this.trade = trade;
        this.user = user;
    }

    @Override
    public void updateState() {
        registerEdit();

        Bukkit.getScheduler().runTask(plugin, () -> {
            updateOwnContent();
            user.getInventory().update();
            updatePartnerInventory();
        });
    }

    @Override
    public void cancelTrade() {
        trade.cancel(user);
    }

    @Override
    public void execute(int slot, ClickType type) {
        switch (slot) {
            case 9 -> changeBid(10, type);
            case 10 -> changeBid(100, type);
            case 11 -> changeBid(1000, type);
            case 12 -> changeBid(10000, type);
            case 40 -> changeState();
        }
    }

    private void registerEdit() {
        if (user.getTradingState().equals(TradingState.TRADING)) return;

        user.setTradingState(TradingState.TRADING);
        trade.getPartner(user).setTradingState(TradingState.TRADING);
        trade.resetCountdown();

        user.getInventory().update();
        trade.getPartner(user).getInventory().update();
    }

    private void updateOwnContent() {
        Inventory inventory = user.getInventory().getInventory();

        ItemStack[] contents = new ItemStack[OWN_SLOT_TO_ITEM_MAP.size()];
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (!OWN_SLOT_TO_ITEM_MAP.containsKey(slot)) continue;
            contents[OWN_SLOT_TO_ITEM_MAP.get(slot)] = inventory.getItem(slot);
        }

        user.setTradeContents(contents);
    }

    private void updatePartnerInventory() {
        trade.getPartner(user).getInventory().update();
    }

    private void changeBid(int amount, ClickType type) {
        amount = Math.abs(amount);
        if (type.equals(ClickType.RIGHT)) amount *= -1;

        int futureBid = user.getCurrentBid() + amount;
        double balance = economy.getBalance(user.getPlayer());

        if (balance < futureBid) {
            user.getPlayer().playSound(user.getPlayer().getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
            return;
        }

        if (futureBid < 0) amount = -user.getCurrentBid();

        user.changeCurrentBid(amount);

        registerEdit();
        user.getInventory().update();
        updatePartnerInventory();
    }

    private void changeState() {

    }

    static {
        OWN_ITEM_TO_SLOT_MAP.put(0, 27);
        OWN_ITEM_TO_SLOT_MAP.put(1, 28);
        OWN_ITEM_TO_SLOT_MAP.put(2, 29);
        OWN_ITEM_TO_SLOT_MAP.put(3, 30);
        OWN_ITEM_TO_SLOT_MAP.put(4, 36);
        OWN_ITEM_TO_SLOT_MAP.put(5, 37);
        OWN_ITEM_TO_SLOT_MAP.put(6, 38);
        OWN_ITEM_TO_SLOT_MAP.put(7, 39);
        OWN_ITEM_TO_SLOT_MAP.put(8, 45);
        OWN_ITEM_TO_SLOT_MAP.put(9, 46);
        OWN_ITEM_TO_SLOT_MAP.put(10, 47);
        OWN_ITEM_TO_SLOT_MAP.put(11, 48);

        OWN_SLOT_TO_ITEM_MAP.putAll(OWN_ITEM_TO_SLOT_MAP.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
    }
}
