package at.tbloder.cukes.container;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

/**
 * Registers Cukes in the Belt inside the container.
 */
public class CukesRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(CukesLifecycle.class);
    }
}
