package de.tmxx.trading.trade.impl;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tmxx.trading.TradingPlugin;
import de.tmxx.trading.module.plugin.PluginLogger;
import de.tmxx.trading.trade.TradeLicenseExaminer;
import de.tmxx.trading.trade.check.TradeLicenseCheck;
import de.tmxx.trading.user.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Project: trading
 * 04.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class TradeLicenseExaminerImpl implements TradeLicenseExaminer {
    private final Logger logger;
    private final List<TradeLicenseCheck> checks = new ArrayList<>();

    @Inject
    TradeLicenseExaminerImpl(@PluginLogger Logger logger) {
        this.logger = logger;

        loadChecks();
    }

    @Override
    public boolean check(User initiator, User partner) {
        for (TradeLicenseCheck check : checks) {
            if (!check.isEnabled()) continue;
            if (!check.check(initiator, partner)) return false;
        }
        return true;
    }

    private void loadChecks() {
        try {
            ClassPath.from(getClass().getClassLoader()).getTopLevelClasses(TradeLicenseCheck.class.getPackageName()).forEach(classInfo -> {
                Class<?> clazz = classInfo.load();
                if (!TradeLicenseCheck.class.isAssignableFrom(clazz) || clazz.equals(TradeLicenseCheck.class)) return;

                Class<? extends TradeLicenseCheck> checkClass = clazz.asSubclass(TradeLicenseCheck.class);
                checks.add(TradingPlugin.unsafe().getInstance(checkClass));
                logger.info("Loaded " + checkClass.getSimpleName());
            });
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while loading trade license check", e);
        }
    }
}
