package de.tmxx.trading.command;

import com.google.inject.Inject;
import de.tmxx.trading.module.plugin.MainConfig;
import de.tmxx.trading.trade.TradeLicenseExaminer;
import de.tmxx.trading.trade.TradingStatus;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

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
    private static final String ACCEPT_COMMAND = "/accepttrade %s";

    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private final FileConfiguration config;

    private final UserRegistry userRegistry;
    private final TradingStatus tradingStatus;
    private final TradeLicenseExaminer examiner;

    @Inject
    TradeCommand(
            @MainConfig FileConfiguration config,
            UserRegistry userRegistry,
            TradingStatus tradingStatus,
            TradeLicenseExaminer examiner
    ) {
        name = config.getString("command.name", "trade");
        aliases.addAll(config.getStringList("command.aliases"));

        this.config = config;
        this.userRegistry = userRegistry;
        this.tradingStatus = tradingStatus;
        this.examiner = examiner;
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

        if (!tradingStatus.isEnabled()) {
            user.sendMessage("trading-disabled");
            return;
        }

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

        if (!examiner.check(user, target)) return;

        if (target.hasRequest(user)) {
            user.sendMessage("command.trade.already-invited");
            return;
        }

        if (user.hasRequest(target)) {
            Bukkit.dispatchCommand(user.getPlayer(), ACCEPT_COMMAND.formatted(target.getName()));
            return;
        }

        target.addRequest(user);

        user.sendMessage("command.trade.sent", target.getName());
        target.sendMessage("command.trade.received", user.getName());
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) return List.of();

        return Bukkit.getOnlinePlayers().stream()
                .filter(online -> online.canSee(player) && !online.equals(player))
                .map(Player::getName)
                .toList();
    }

    @Override
    public @Nullable String permission() {
        return "trading.command.trade";
    }
}
