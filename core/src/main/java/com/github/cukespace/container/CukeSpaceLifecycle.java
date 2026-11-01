package com.github.cukespace.container;

import com.github.cukespace.api.event.ScenarioEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Captures what Cucumber needs from Arquillian while a test method runs: the
 * test enrichers (to inject glue instances) and the event bus (to publish
 * {@link ScenarioEvent}s). Registered on the client and in the container.
 */
public class CukeSpaceLifecycle {
    private static final Logger LOGGER = Logger.getLogger(CukeSpaceLifecycle.class.getName());

    private static volatile Collection<TestEnricher> enrichers = List.of();
    private static volatile Event<ScenarioEvent> scenarioEvents;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<ScenarioEvent> events;

    public void before(@Observes final Before before) {
        // reload them for each test: enrichers may depend on the deployment
        enrichers = List.copyOf(serviceLoader.get().all(TestEnricher.class));
        scenarioEvents = events;
    }

    public void after(@Observes final After after) {
        enrichers = List.of();
        scenarioEvents = null;
    }

    /**
     * Injects Arquillian resources into a glue instance.
     *
     * @param instance the glue instance
     * @param <T>      its type
     * @return the instance
     */
    public static <T> T enrich(final T instance) {
        for (final TestEnricher enricher : enrichers) {
            try {
                enricher.enrich(instance);
            } catch (final RuntimeException e) {
                // one failing enricher (e.g. no CDI in this deployment) must not break the others
                LOGGER.log(Level.FINE, e, () -> "Enricher " + enricher + " failed for " + instance.getClass().getName());
            }
        }
        return instance;
    }

    static void fire(final ScenarioEvent event) {
        final Event<ScenarioEvent> current = scenarioEvents;
        if (current != null) {
            current.fire(event);
        }
    }
}
