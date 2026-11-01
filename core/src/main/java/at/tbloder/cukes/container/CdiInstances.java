package at.tbloder.cukes.container;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates glue instances as non-contextual CDI instances, so the container
 * performs all injections it supports ({@code @Inject} and, in a Jakarta EE
 * server, {@code @EJB}, {@code @Resource}, {@code @PersistenceContext}, ...)
 * and calls {@code @PostConstruct}/{@code @PreDestroy}.
 * <p>
 * Only loaded when the CDI API is available.
 */
final class CdiInstances {
    private static final Logger LOGGER = Logger.getLogger(CdiInstances.class.getName());

    private final BeanManager beanManager;
    private final List<Runnable> destroyers = new ArrayList<>();

    private CdiInstances(final BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    /**
     * Returns the CDI instance factory or {@code null} without an active CDI container.
     *
     * @return the CDI instance factory or {@code null} without an active CDI container
     */
    static CdiInstances lookup() {
        try {
            return new CdiInstances(CDI.current().getBeanManager());
        } catch (final RuntimeException | LinkageError e) { // no CDI provider or no container for this deployment
            LOGGER.log(Level.FINE, "CDI not available, glue instances are only enriched by Arquillian", e);
            return null;
        }
    }

    <T> T create(final Class<T> type, final Supplier<T> fallback) {
        final InjectionTarget<T> target;
        try {
            target = beanManager.getInjectionTargetFactory(beanManager.createAnnotatedType(type)).createInjectionTarget(null);
        } catch (final RuntimeException e) { // e.g. a class CDI can't handle
            LOGGER.log(Level.FINE, e, () -> "CDI can't create " + type.getName());
            return fallback.get();
        }
        final CreationalContext<T> context = beanManager.createCreationalContext(null);
        final T instance = target.produce(context);
        target.inject(instance, context);
        target.postConstruct(instance);
        destroyers.add(() -> {
            target.preDestroy(instance);
            target.dispose(instance);
            context.release();
        });
        return instance;
    }

    void destroyAll() {
        for (final Runnable destroyer : destroyers) {
            try {
                destroyer.run();
            } catch (final RuntimeException e) {
                LOGGER.log(Level.WARNING, "Can't destroy a glue instance", e);
            }
        }
        destroyers.clear();
    }
}
