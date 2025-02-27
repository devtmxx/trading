package de.tmxx.trading.helper.impl;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import de.tmxx.trading.TradingPlugin;
import de.tmxx.trading.command.PluginCommand;
import de.tmxx.trading.helper.CommandHelper;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Project: trading
 * 27.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class CommandHelperImpl implements CommandHelper {
    private final JavaPlugin plugin;

    @Inject
    CommandHelperImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerCommands() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();

            loadCommands().forEach(command -> commands.register(command.name(), command.aliases(), command));
        });
    }

    private Set<PluginCommand> loadCommands() {
        Set<PluginCommand> commands = new HashSet<>();

        try {
            ClassPath.from(getClass().getClassLoader()).getTopLevelClassesRecursive(PluginCommand.class.getPackageName()).forEach(classInfo -> {
                PluginCommand command = getCommandOrNull(classInfo.load());
                if (command == null) return;

                commands.add(command);
            });
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while registering commands", e);
        }

        return commands;
    }

    private PluginCommand getCommandOrNull(Class<?> clazz) {
        if (!PluginCommand.class.isAssignableFrom(clazz)) return null;
        if (clazz.getSimpleName().equals(PluginCommand.class.getSimpleName())) return null;

        Class<? extends PluginCommand> commandClass = clazz.asSubclass(PluginCommand.class);
        return TradingPlugin.unsafe().getInstance(commandClass);
    }
}
