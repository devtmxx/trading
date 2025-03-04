package de.tmxx.trading.command;

import com.google.inject.Inject;
import de.tmxx.trading.i18n.I18n;
import de.tmxx.trading.trade.TradingStatus;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class ToggleTradingCommand implements PluginCommand {
    private final I18n i18n;
    private final UserRegistry registry;
    private final TradingStatus tradingStatus;

    @Inject
    ToggleTradingCommand(I18n i18n, UserRegistry registry, TradingStatus tradingStatus) {
        this.i18n = i18n;
        this.registry = registry;
        this.tradingStatus = tradingStatus;
    }

    @Override
    public String name() {
        return "toggletrading";
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        tradingStatus.toggle();

        if (!tradingStatus.isEnabled()) cancelAllActiveTrades();

        String status = tradingStatus.isEnabled() ? "enabled" : "disabled";
        if (stack.getSender() instanceof Player player) {
            player.sendMessage(i18n.translate(player.locale(), "command.toggletrading." + status));
        } else {
            stack.getSender().sendMessage("Trading has been " + status + " for all players.");
        }
    }

    @Override
    public @Nullable String permission() {
        return "trading.command.toggle";
    }

    private void cancelAllActiveTrades() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            User user = registry.get(player);
            if (user == null || user.getTrade() == null) return;

            user.getTrade().cancel(null);
        });
    }
}
