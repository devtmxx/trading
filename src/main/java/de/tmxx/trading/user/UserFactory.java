package de.tmxx.trading.user;

import org.bukkit.entity.Player;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface UserFactory {
    User create(Player player);
}
