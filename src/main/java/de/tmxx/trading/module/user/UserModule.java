package de.tmxx.trading.module.user;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.tmxx.trading.user.User;
import de.tmxx.trading.user.UserFactory;
import de.tmxx.trading.user.UserRegistry;
import de.tmxx.trading.user.impl.UserImpl;
import de.tmxx.trading.user.impl.UserRegistryImpl;

/**
 * Project: trading
 * 26.02.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class UserModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserRegistry.class).to(UserRegistryImpl.class);

        install(new FactoryModuleBuilder().implement(User.class, UserImpl.class).build(UserFactory.class));
    }
}
