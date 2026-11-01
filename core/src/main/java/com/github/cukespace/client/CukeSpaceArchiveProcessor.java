package com.github.cukespace.client;

import com.github.cukespace.CukeSpace;
import com.github.cukespace.api.event.ScenarioEvent;
import com.github.cukespace.container.CukeSpaceRemoteExtension;
import com.github.cukespace.shared.ArquillianConfiguration;
import com.github.cukespace.shared.CukeSpaceOptions;
import com.github.cukespace.shared.DeploymentIndex;
import io.cucumber.core.backend.discovery.GlueDiscoverySelectorResolver;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Prepares the deployment of a CukeSpace test: features, glue classes,
 * Cucumber properties, the Cucumber libraries and the container side of CukeSpace.
 * <p>
 * A plain {@link JavaArchive} deployment is left untouched: it is used by embedded
 * containers (e.g. Weld) which run with the class path of the test.
 */
public class CukeSpaceArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger LOGGER = Logger.getLogger(CukeSpaceArchiveProcessor.class.getName());

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        final Class<?> javaClass = testClass.getJavaClass();
        if (!CukeSpace.isCukeSpaceTest(javaClass) || !supportsLibraries(applicationArchive)) {
            return;
        }

        final ClassLoader loader = loader(javaClass);
        final Supplier<ClassLoader> loaderSupplier = () -> loader;
        final Map<String, String> properties = CukeSpaceOptions.collectProperties(ArquillianConfiguration.properties());
        final RuntimeOptions options = CukeSpaceOptions.create(javaClass, properties);

        final List<Feature> features = new FeaturePathFeatureSupplier(loaderSupplier, options, new FeatureParser(UUID::randomUUID)).get();
        final List<Class<?>> glue = new GlueDiscoverySelectorResolver(new ClasspathScanner(loaderSupplier), CukeSpaceArchiveProcessor::isGlue)
                .resolve(options.getGlueDiscoveryRequest())
                .toList();
        LOGGER.fine(() -> "CukeSpace deployment of " + javaClass.getName() + ": features=" + features.size() + ", glue=" + glue);

        final Archive<?> target = findArchiveOf(applicationArchive, javaClass);
        final LibraryContainer<?> libraries = (LibraryContainer<?>) target;

        libraries.addAsLibrary(resources(features, glue, properties));
        libraries.addAsLibrary(containerJar());
        addMissingGlue(target, libraries, glue, javaClass);
        for (final File jar : CucumberLibraries.find(loader)) {
            if (!target.contains(libraryPath(target, jar.getName()))) {
                libraries.addAsLibrary(jar);
            }
        }
    }

    private static JavaArchive resources(final List<Feature> features, final List<Class<?>> glue, final Map<String, String> properties) {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "cukespace-resources.jar");
        final List<DeploymentIndex.FeatureEntry> entries = new ArrayList<>(features.size());
        for (int i = 0; i < features.size(); i++) {
            final Feature feature = features.get(i);
            final String path = DeploymentIndex.FEATURES + i + ".feature";
            jar.addAsResource(new ByteArrayAsset(feature.getSource().getBytes(StandardCharsets.UTF_8)), path);
            entries.add(new DeploymentIndex.FeatureEntry(path, feature.getUri()));
        }
        final DeploymentIndex index = new DeploymentIndex(entries, glue.stream().map(Class::getName).toList(), properties);
        jar.addAsResource(new StringAsset(index.toPropertiesString()), DeploymentIndex.INDEX);
        return jar;
    }

    private static JavaArchive containerJar() {
        return ShrinkWrap.create(JavaArchive.class, "cukespace-core.jar")
                .addPackage(CukeSpace.class.getPackage())
                .addPackage(ScenarioEvent.class.getPackage())
                .addPackage(CukeSpaceRemoteExtension.class.getPackage())
                .addPackage(DeploymentIndex.class.getPackage())
                .addAsServiceProvider(RemoteLoadableExtension.class, CukeSpaceRemoteExtension.class);
    }

    // glue classes the deployment does not contain yet (steps are test code, users rarely add them)
    private static void addMissingGlue(final Archive<?> target, final LibraryContainer<?> libraries,
                                       final List<Class<?>> glue, final Class<?> testClass) {
        final Set<Class<?>> testHierarchy = new LinkedHashSet<>();
        for (Class<?> c = testClass; c != null && c != Object.class; c = c.getSuperclass()) {
            testHierarchy.add(c); // Arquillian adds the test class itself
        }
        final Set<Class<?>> missing = new LinkedHashSet<>();
        for (final Class<?> glueClass : glue) {
            for (Class<?> c = glueClass; c != null && c != Object.class; c = c.getSuperclass()) {
                if (!testHierarchy.contains(c) && !isProvided(c) && !contains(target, c)) {
                    missing.add(c);
                }
            }
        }
        if (missing.isEmpty()) {
            return;
        }
        final Class<?>[] classes = missing.toArray(new Class<?>[0]);
        if (target instanceof WebArchive war) {
            // module classes: containers only process injection annotations (@EJB, @Resource) of scanned classes
            war.addClasses(classes);
        } else {
            libraries.addAsLibrary(ShrinkWrap.create(JavaArchive.class, "cukespace-glue.jar").addClasses(classes));
        }
    }

    private static final Set<String> CUKESPACE_PACKAGES = Set.of(
            CukeSpace.class.getPackageName(), ScenarioEvent.class.getPackageName(),
            CukeSpaceRemoteExtension.class.getPackageName(), DeploymentIndex.class.getPackageName(),
            CukeSpaceArchiveProcessor.class.getPackageName());

    private static boolean isProvided(final Class<?> c) {
        return c.getName().startsWith("io.cucumber.") || CUKESPACE_PACKAGES.contains(c.getPackageName());
    }

    static boolean isGlue(final Class<?> clazz) {
        try {
            return Stream.concat(Stream.of(clazz.getDeclaredMethods()), Stream.of(clazz.getMethods()))
                    .map(Method::getAnnotations)
                    .flatMap(Stream::of)
                    .map(Annotation::annotationType)
                    .anyMatch(type -> type.getName().startsWith("io.cucumber.java."));
        } catch (final LinkageError e) { // optional dependencies missing
            return false;
        }
    }

    private static Archive<?> findArchiveOf(final Archive<?> topArchive, final Class<?> testClass) {
        if (contains(topArchive, testClass)) {
            return topArchive;
        }
        for (final Node node : topArchive.getContent(path -> path.get().endsWith(".war") || path.get().endsWith(".jar")).values()) {
            if (node.getAsset() instanceof ArchiveAsset asset
                    && supportsLibraries(asset.getArchive())
                    && contains(asset.getArchive(), testClass)) {
                return asset.getArchive();
            }
        }
        return topArchive;
    }

    private static boolean supportsLibraries(final Archive<?> archive) {
        // JavaArchive implements LibraryContainer too, but does not support libraries
        return archive instanceof WebArchive || archive instanceof EnterpriseArchive;
    }

    private static boolean contains(final Archive<?> archive, final Class<?> clazz) {
        final String path = clazz.getName().replace('.', '/') + ".class";
        return archive.contains(path) || archive.contains("/WEB-INF/classes/" + path);
    }

    private static String libraryPath(final Archive<?> archive, final String name) {
        return (archive instanceof WebArchive ? "/WEB-INF/lib/" : "/lib/") + name;
    }

    private static ClassLoader loader(final Class<?> testClass) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl != null ? tccl : testClass.getClassLoader();
    }
}
