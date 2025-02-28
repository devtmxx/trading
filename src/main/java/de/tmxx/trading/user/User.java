package de.tmxx.trading.user;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface User {
    Player getPlayer();
    String getName();
    UUID getUniqueId();
    String getIp();
    void sendMessage(String key, Object... args);
    Component translate(String key, Object... args);
    List<Component> translateLore(String key, Object... args);

    void addRequest(User requestedUser);
    boolean hasRequest(User requestedUser);
    void invalidateRequest(User requestedUser);
}
