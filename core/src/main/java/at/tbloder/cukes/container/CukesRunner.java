package at.tbloder.cukes.container;

import at.tbloder.cukes.shared.ArquillianConfiguration;
import at.tbloder.cukes.shared.CukesOptions;
import at.tbloder.cukes.shared.DeploymentIndex;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.discovery.GlueDiscoveryFilter;
import io.cucumber.core.backend.discovery.GlueDiscoveryRequest;
import io.cucumber.core.backend.discovery.GlueDiscoverySelector;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.SingletonObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import org.opentest4j.MultipleFailuresError;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Runs the Cucumber features of one test instance, wherever the test method runs.
 * Use {@link at.tbloder.cukes.Cukes#runFeatures(Object)}.
 */
public final class CukesRunner {
    private static final Logger LOGGER = Logger.getLogger(CukesRunner.class.getName());

    private final Object testInstance;

    public CukesRunner(final Object testInstance) {
        this.testInstance = testInstance;
    }

    public void run() {
        final Class<?> testClass = testInstance.getClass();
        final ClassLoader loader = classLoader(testClass);
        final Supplier<ClassLoader> loaderSupplier = () -> loader;

        // inside a deployment prepared by the client: use its features, glue and properties
        final Optional<DeploymentIndex> index = DeploymentIndex.load(loader);
        final Map<String, String> properties = index
                .map(DeploymentIndex::getProperties)
                .orElseGet(() -> CukesOptions.collectProperties(ArquillianConfiguration.properties()));
        final RuntimeOptions options = CukesOptions.create(testClass, properties);

        final ObjectFactorySupplier objectFactories = objectFactories(options, loaderSupplier);
        final ScenarioListener listener = new ScenarioListener();
        final Runtime.Builder builder = Runtime.builder()
                .withRuntimeOptions(options)
                .withClassLoader(loaderSupplier)
                .withObjectFactorySupplier(objectFactories)
                .withAdditionalPlugins(listener);
        index.ifPresent(i -> builder
                .withFeatureSupplier(new IndexedFeatureSupplier(loader, i.getFeatures()))
                .withBackendSupplier(() -> explicitGlue(new BackendServiceLoader(loaderSupplier, objectFactories).get(), i.getGlueClasses())));

        builder.build().run();

        if (listener.getScenarioCount() == 0) {
            LOGGER.warning(() -> "No scenario executed for " + testClass.getName()
                    + " (features: " + options.getFeaturePaths() + ", tags: " + options.getTagExpressions() + ")");
        }
        final List<? extends Throwable> failures = listener.getFailures();
        if (!failures.isEmpty()) {
            throw new MultipleFailuresError(heading(listener, failures.size()), failures);
        }
    }

    private ObjectFactorySupplier objectFactories(final RuntimeOptions options, final Supplier<ClassLoader> loader) {
        if (options.getObjectFactoryClass() != null) { // explicitly configured, e.g. cucumber-jakarta-cdi
            final ObjectFactoryServiceLoader serviceLoader = new ObjectFactoryServiceLoader(loader, options);
            return options.isMultiThreaded()
                    ? new ThreadLocalObjectFactorySupplier(serviceLoader)
                    : new SingletonObjectFactorySupplier(serviceLoader);
        }
        if (options.isMultiThreaded()) {
            final ThreadLocal<ObjectFactory> perThread = ThreadLocal.withInitial(() -> new ArquillianObjectFactory(testInstance));
            return perThread::get;
        }
        final ObjectFactory factory = new ArquillianObjectFactory(testInstance);
        return () -> factory;
    }

    private static String heading(final ScenarioListener listener, final int failed) {
        final StringBuilder heading = new StringBuilder()
                .append(failed).append(" of ").append(listener.getScenarioCount()).append(" scenarios failed");
        if (!listener.getSnippets().isEmpty()) {
            heading.append(System.lineSeparator())
                    .append("You can implement the missing steps with these snippets:")
                    .append(System.lineSeparator());
            listener.getSnippets().forEach(s -> heading.append(System.lineSeparator()).append(s).append(System.lineSeparator()));
        }
        return heading.toString();
    }

    private static ClassLoader classLoader(final Class<?> testClass) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl != null ? tccl : testClass.getClassLoader();
    }

    // class path scanning is not reliable in application servers, use the classes found by the client
    private static Collection<? extends Backend> explicitGlue(final Collection<? extends Backend> backends, final List<String> glueClasses) {
        return backends.stream().map(b -> new ExplicitGlueBackend(b, glueClasses)).toList();
    }

    private static final class ExplicitGlueBackend implements Backend {
        private final Backend delegate;
        private final List<String> glueClasses;

        private ExplicitGlueBackend(final Backend delegate, final List<String> glueClasses) {
            this.delegate = delegate;
            this.glueClasses = glueClasses;
        }

        @Override
        public void loadGlue(final Glue glue, final GlueDiscoveryRequest request) {
            delegate.loadGlue(glue, GlueDiscoveryRequest.builder()
                    .options(request.getOptions())
                    .filters(request.getFiltersByType(GlueDiscoveryFilter.class))
                    .selectors(glueClasses.stream().map(GlueDiscoverySelector::selectClass).toList())
                    .build());
        }

        @Override
        public void buildWorld() {
            delegate.buildWorld();
        }

        @Override
        public void disposeWorld() {
            delegate.disposeWorld();
        }

        @Override
        public Snippet getSnippet() {
            return delegate.getSnippet();
        }
    }
}
