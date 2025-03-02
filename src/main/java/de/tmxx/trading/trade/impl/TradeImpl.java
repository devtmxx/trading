package de.tmxx.trading.trade.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeImpl implements Trade {
    private static final int START_COUNTDOWN = 5;

    private final JavaPlugin plugin;
    private final User initiator;
    private final User partner;

    private int countdown = START_COUNTDOWN;
    private boolean cancelled = false;
    private BukkitTask completionTask = null;

    @Inject
    TradeImpl(JavaPlugin plugin, @Assisted("initiator") User initiator, @Assisted("partner") User partner) {
        this.plugin = plugin;
        this.initiator = initiator;
        this.partner = partner;
    }

    @Override
    public void start() {
        initiator.getInventory().open();
        partner.getInventory().open();
    }

    @Override
    public void cancel(User cancelledBy) {
        if (cancelled) return;
        cancelled = true;

        if (completionTask != null) {
            completionTask.cancel();
            completionTask = null;
        }

        forBoth(user -> {
            user.sendMessage("trade-cancelled", cancelledBy.getName());
            user.getPlayer().closeInventory();
            user.setInventory(null);
            user.setTradingState(TradingState.TRADING);
            user.resetCurrentBid();
            user.returnItems();
        });
    }

    @Override
    public User getPartner(User user) {
        return user.equals(initiator) ? partner : initiator;
    }

    @Override
    public void resetCountdown() {
        countdown = START_COUNTDOWN;
    }

    @Override
    public int getCountdown() {
        return countdown;
    }

    @Override
    public void checkTradingStates() {
        if (initiator.getTradingState().equals(TradingState.ACCEPTED) && partner.getTradingState().equals(TradingState.ACCEPTED)) {
            startCompletion();
        }
    }

    private void forBoth(Consumer<User> action) {
        action.accept(initiator);
        action.accept(partner);
    }

    private void startCompletion() {
        if (completionTask != null) return;

        forBoth(user -> user.setTradingState(TradingState.COMPLETION));
        forBoth(user -> user.getInventory().update());

        completionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            countdown--;

            if (countdown == 0) {
                // TODO: finish the trade

                completionTask.cancel();
                completionTask = null;
                return;
            }

            forBoth(user -> user.getInventory().update());
        }, 20L, 20L);
    }
}
