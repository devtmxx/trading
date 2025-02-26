package de.tmxx.trading.user;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface UserRegistry {
    void register(User user);
    User get(UUID uniqueId);
    User get(String name);
    User get(Player player);
    void unregister(UUID uniqueId);
}
