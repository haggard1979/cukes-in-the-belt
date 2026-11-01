package com.github.cukespace.client;

import com.github.cukespace.container.CukeSpaceLifecycle;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Registers CukeSpace on the client.
 */
public class CukeSpaceExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.service(ApplicationArchiveProcessor.class, CukeSpaceArchiveProcessor.class)
                .observer(ConfigurationObserver.class)
                .observer(CukeSpaceLifecycle.class);
    }
}
