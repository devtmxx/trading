package de.tmxx.trading.trade.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.user.User;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
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
    private final Economy economy;
    private final User initiator;
    private final User partner;

    private int countdown = START_COUNTDOWN;
    private boolean done = false;
    private BukkitTask completionTask = null;

    @Inject
    TradeImpl(JavaPlugin plugin, Economy economy, @Assisted("initiator") User initiator, @Assisted("partner") User partner) {
        this.plugin = plugin;
        this.economy = economy;
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
        if (done) return;
        done = true;

        resetCountdown();

        forBoth(user -> {
            user.sendMessage("trade-cancelled", cancelledBy.getName());
            user.getPlayer().closeInventory();
            user.setInventory(null);
            user.setTradingState(TradingState.TRADING);
            user.resetCurrentBid();
            user.giveItems(user);
        });
    }

    @Override
    public User getPartner(User user) {
        return user.equals(initiator) ? partner : initiator;
    }

    @Override
    public void resetCountdown() {
        if (completionTask != null) {
            completionTask.cancel();
            completionTask = null;
        }

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
                finish();

                completionTask.cancel();
                completionTask = null;
                return;
            }

            forBoth(user -> user.getInventory().update());
        }, 20L, 20L);
    }

    private void finish() {
        if (done) return;
        done = true;

        if (completionTask != null) completionTask.cancel();

        forBoth(user -> {
            user.getPlayer().closeInventory();
            user.setTradingState(TradingState.TRADING);
            user.setInventory(null);
        });

        if (!exchangeMoney()) {
            forBoth(user -> {
                user.sendMessage("trade-cancelled-currency");
                user.resetCurrentBid();
                user.giveItems(user);
            });
            return;
        }

        initiator.giveItems(partner);
        partner.giveItems(initiator);

        forBoth(user -> {
            user.resetCurrentBid();
            user.sendMessage("trade-success", getPartner(user).getName());
        });
    }

    private boolean exchangeMoney() {
        return sendMoney(initiator, partner) && sendMoney(partner, initiator);
    }

    private boolean sendMoney(User from, User to) {
        if (from.getCurrentBid() == 0) return true;

        EconomyResponse response = economy.withdrawPlayer(from.getPlayer(), from.getCurrentBid());
        if (!response.transactionSuccess()) return false;

        response = economy.depositPlayer(to.getPlayer(), from.getCurrentBid());
        if (!response.transactionSuccess()) {
            economy.depositPlayer(from.getPlayer(), from.getCurrentBid());
            return false;
        }

        return true;
    }
}
