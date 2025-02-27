package de.tmxx.trading.command;

import io.papermc.paper.command.brigadier.BasicCommand;

import java.util.Collection;
import java.util.List;

/**
 * Project: trading
 * 27.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface PluginCommand extends BasicCommand {
    String name();

    default Collection<String> aliases() {
        return List.of();
    }
}
