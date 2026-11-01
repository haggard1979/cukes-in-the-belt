package at.tbloder.cukes.client;

import at.tbloder.cukes.container.CukesLifecycle;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Registers Cukes in the Belt on the client.
 */
public class CukesExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.service(ApplicationArchiveProcessor.class, CukesArchiveProcessor.class)
                .observer(ConfigurationObserver.class)
                .observer(CukesLifecycle.class);
    }
}
