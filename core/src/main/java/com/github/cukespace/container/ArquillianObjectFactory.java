package com.github.cukespace.container;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the glue instances:
 * <ul>
 * <li>the running test instance for steps declared in the test class (or its super classes),</li>
 * <li>a new instance per scenario for every other glue class. When CDI is available the
 * instance is created by the container (non-contextual instance: {@code @Inject}, {@code @EJB},
 * {@code @Resource}, ... as supported by the server), then it is enriched by Arquillian
 * ({@code @ArquillianResource}, ...).</li>
 * </ul>
 */
final class ArquillianObjectFactory implements ObjectFactory {
    private static final boolean CDI_API = isCdiApiAvailable();

    private final Object testInstance;
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private CdiInstances cdi;

    ArquillianObjectFactory(final Object testInstance) {
        this.testInstance = testInstance;
    }

    @Override
    public void start() {
        cdi = CDI_API ? CdiInstances.lookup() : null;
    }

    @Override
    public void stop() {
        instances.clear();
        if (cdi != null) {
            cdi.destroyAll();
            cdi = null;
        }
    }

    @Override
    public boolean addClass(final Class<?> glueClass) {
        return true;
    }

    @Override
    public <T> T getInstance(final Class<T> glueClass) {
        if (glueClass.isInstance(testInstance)) {
            return glueClass.cast(testInstance);
        }
        return glueClass.cast(instances.computeIfAbsent(glueClass, this::create));
    }

    private <T> T create(final Class<T> glueClass) {
        final T instance = cdi != null ? cdi.create(glueClass, () -> newInstance(glueClass)) : newInstance(glueClass);
        return CukeSpaceLifecycle.enrich(instance);
    }

    private static boolean isCdiApiAvailable() {
        try {
            Class.forName("jakarta.enterprise.inject.spi.CDI", false, ArquillianObjectFactory.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private static <T> T newInstance(final Class<T> glueClass) {
        try {
            final Constructor<T> constructor = glueClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final NoSuchMethodException e) {
            throw new CucumberBackendException(glueClass.getName() + " needs a no-argument constructor to be used as glue", e);
        } catch (final InvocationTargetException e) {
            throw new CucumberBackendException("Can't create " + glueClass.getName(), e.getCause());
        } catch (final ReflectiveOperationException e) {
            throw new CucumberBackendException("Can't create " + glueClass.getName(), e);
        }
    }
}
