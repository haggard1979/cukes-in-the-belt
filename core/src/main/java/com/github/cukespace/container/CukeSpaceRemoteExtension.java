package com.github.cukespace.container;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

/**
 * Registers CukeSpace inside the container.
 */
public class CukeSpaceRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(CukeSpaceLifecycle.class);
    }
}
