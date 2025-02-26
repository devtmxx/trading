package de.tmxx.trading.helper.impl;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import de.tmxx.trading.TradingPlugin;
import de.tmxx.trading.helper.ListenerHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class ListenerHelperImpl implements ListenerHelper {
    private static final String LISTENER_PACKAGE = "de.tmxx.trading.listener";
    private final JavaPlugin plugin;

    @Inject
    ListenerHelperImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        try {
            ClassPath.from(getClass().getClassLoader()).getTopLevelClassesRecursive(LISTENER_PACKAGE).forEach(classInfo -> {
                Class<?> clazz = classInfo.load();
                if (!Listener.class.isAssignableFrom(clazz)) return;

                Class<? extends Listener> listenerClass = clazz.asSubclass(Listener.class);
                Bukkit.getPluginManager().registerEvents(TradingPlugin.unsafe().getInstance(listenerClass), plugin);
            });
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while registering listeners", e);
        }
    }
}
