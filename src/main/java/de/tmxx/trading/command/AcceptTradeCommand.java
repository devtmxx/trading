package de.tmxx.trading.command;

import com.google.inject.Inject;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradeFactory;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class AcceptTradeCommand implements PluginCommand {
    private final UserRegistry userRegistry;
    private final TradeFactory tradeFactory;

    @Inject
    AcceptTradeCommand(UserRegistry userRegistry, TradeFactory tradeFactory) {
        this.userRegistry = userRegistry;
        this.tradeFactory = tradeFactory;
    }

    @Override
    public String name() {
        return "accepttrade";
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage("You must be a player to use this command!");
            return;
        }

        User user = userRegistry.get(player);
        if (user == null) return;

        if (args.length == 0) {
            user.sendMessage("command.accepttrade.help");
            return;
        }

        User target = userRegistry.get(args[0]);
        if (target == null) {
            user.sendMessage("command.accepttrade.not-found");
            return;
        }

        if (!user.hasRequest(target)) {
            user.sendMessage("command.accepttrade.no-invitation");
            return;
        }

        user.invalidateRequest(target);

        Trade trade = tradeFactory.create(target, user);
        user.setInventory(tradeFactory.createInventory(trade, user));
        target.setInventory(tradeFactory.createInventory(trade, target));
        trade.start();
    }
}
