package de.tmxx.trading.trade.inventory.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradeFactory;
import de.tmxx.trading.trade.inventory.TradeInventory;
import de.tmxx.trading.trade.inventory.TradeInventoryBuilder;
import de.tmxx.trading.trade.inventory.TradeInventoryHandler;
import de.tmxx.trading.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeInventoryImpl implements TradeInventory, InventoryHolder, Listener {
    private static final Set<InventoryAction> ALLOWED_ACTIONS = Set.of(
            InventoryAction.NOTHING,
            InventoryAction.PICKUP_ALL,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.SWAP_WITH_CURSOR
    );
    private static final Set<Integer> ALLOWED_SLOTS_TO_MOVE = Set.of(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48);

    private final JavaPlugin plugin;
    private final TradeInventoryBuilder builder;
    private final TradeInventoryHandler handler;
    private final User user;

    private Inventory inventory = null;

    @Inject
    TradeInventoryImpl(JavaPlugin plugin, TradeFactory factory, @Assisted Trade trade, @Assisted User user) {
        this.plugin = plugin;
        this.builder = factory.createInventoryBuilder(trade, user);
        this.handler = factory.createInventoryHandler(trade, user);
        this.user = user;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) inventory = builder.newInventory(this);
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getInventory().getHolder(false) != this) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getUniqueId().equals(user.getUniqueId())) return;

        if (!ALLOWED_ACTIONS.contains(event.getAction())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        // allow all clicks in the players own inventory
        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) return;

        if (!ALLOWED_SLOTS_TO_MOVE.contains(event.getRawSlot())) {
            event.setResult(Event.Result.DENY);
            handler.execute(event.getRawSlot(), event.getClick());
            return;
        }

        handler.updateState();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) != this) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getUniqueId().equals(user.getUniqueId())) return;

        for (Integer slot : event.getNewItems().keySet()) {
            if (ALLOWED_SLOTS_TO_MOVE.contains(slot)) continue;

            event.setResult(Event.Result.DENY);
            return;
        }

        handler.updateState();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().getUniqueId().equals(user.getUniqueId())) return;

        handler.cancelTrade();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(user.getUniqueId())) return;

        handler.cancelTrade();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void open() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        user.getPlayer().openInventory(getInventory());
    }

    @Override
    public void update() {
        builder.updateContent();
    }
}
