package de.tmxx.trading.command;

import com.google.inject.Inject;
import de.tmxx.trading.module.plugin.MainConfig;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Project: trading
 * 27.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeCommand implements PluginCommand {
    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private final FileConfiguration config;

    private final UserRegistry userRegistry;

    @Inject
    TradeCommand(@MainConfig FileConfiguration config, UserRegistry userRegistry) {
        name = config.getString("command.name", "trade");
        aliases.addAll(config.getStringList("command.aliases"));

        this.config = config;
        this.userRegistry = userRegistry;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Collection<String> aliases() {
        return aliases;
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
            user.sendMessage("command.trade.help");
            return;
        }

        String name = args[0];
        if (name.equalsIgnoreCase(user.getName())) {
            user.sendMessage("command.trade.no-self-trade");
            return;
        }

        User target = userRegistry.get(name);
        if (target == null) {
            user.sendMessage("command.trade.not-found");
            return;
        }

        boolean noTradingWithMatchingIp = config.getBoolean("no-trading-with-matching-ip", true);
        if (noTradingWithMatchingIp && user.getIp().equalsIgnoreCase(target.getIp())) {
            user.sendMessage("command.trade.no-trading-with-matching-ip");
            return;
        }

        // TODO: start the actual trading
    }
}
