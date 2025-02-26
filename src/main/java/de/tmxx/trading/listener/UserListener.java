package de.tmxx.trading.listener;

import com.google.inject.Inject;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserFactory;
import de.tmxx.trading.user.UserRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class UserListener implements Listener {
    private final UserRegistry registry;
    private final UserFactory factory;

    @Inject
    UserListener(UserRegistry registry, UserFactory factory) {
        this.registry = registry;
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = factory.create(event.getPlayer());
        registry.register(user);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        registry.unregister(event.getPlayer().getUniqueId());
    }
}
