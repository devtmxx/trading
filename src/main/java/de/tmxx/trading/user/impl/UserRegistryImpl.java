package de.tmxx.trading.user.impl;

import com.google.inject.Singleton;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserRegistry;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class UserRegistryImpl implements UserRegistry {
    private static final Map<UUID, User> UUID_USER_MAP = new HashMap<>();
    private static final Map<String, User> NAME_USER_MAP = new HashMap<>();

    @Override
    public void register(User user) {
        UUID_USER_MAP.put(user.getUniqueId(), user);
        NAME_USER_MAP.put(user.getName().toLowerCase(), user);
    }

    @Override
    public User get(UUID uniqueId) {
        return UUID_USER_MAP.get(uniqueId);
    }

    @Override
    public User get(String name) {
        return NAME_USER_MAP.get(name.toLowerCase());
    }

    @Override
    public User get(Player player) {
        return UUID_USER_MAP.get(player.getUniqueId());
    }

    @Override
    public void unregister(UUID uniqueId) {
        User user = UUID_USER_MAP.remove(uniqueId);
        if (user == null) return;

        NAME_USER_MAP.remove(user.getName().toLowerCase());
    }
}
