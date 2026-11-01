package at.tbloder.cukes.client;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finds the Cucumber jars on the client class path, they are added to the deployment.
 */
final class CucumberLibraries {
    private static final Logger LOGGER = Logger.getLogger(CucumberLibraries.class.getName());

    // runners of other test frameworks are not needed (and the JUnit Platform engine would be picked up in the container)
    private static final List<String> EXCLUDED_PACKAGES = List.of(
            "io/cucumber/junit/", "io/cucumber/testng/");

    private static volatile List<File> cache;

    private CucumberLibraries() {
    }

    static List<File> find(final ClassLoader loader) {
        List<File> result = cache;
        if (result == null) {
            result = scan(loader);
            cache = result;
        }
        return result;
    }

    private static List<File> scan(final ClassLoader loader) {
        final Set<File> jars = new LinkedHashSet<>();
        try {
            final Enumeration<URL> manifests = loader.getResources(JarFile.MANIFEST_NAME);
            while (manifests.hasMoreElements()) {
                final File jar = toJar(manifests.nextElement());
                if (jar != null && isCucumberJar(jar)) {
                    jars.add(jar);
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Can't find the Cucumber libraries", e);
        }
        return new ArrayList<>(jars);
    }

    private static File toJar(final URL manifest) {
        if (!"jar".equals(manifest.getProtocol())) {
            return null;
        }
        try {
            final URL jarUrl = ((JarURLConnection) manifest.openConnection()).getJarFileURL();
            if (!"file".equals(jarUrl.getProtocol())) {
                return null;
            }
            final File file = new File(jarUrl.toURI());
            return file.isFile() ? file : null;
        } catch (final IOException | URISyntaxException | IllegalArgumentException e) {
            LOGGER.log(Level.FINE, e, () -> "Ignoring " + manifest);
            return null;
        }
    }

    private static boolean isCucumberJar(final File file) {
        try (JarFile jar = new JarFile(file)) {
            final boolean[] cucumber = {false};
            final boolean[] excluded = {false};
            jar.stream().map(e -> e.getName()).forEach(name -> {
                if (name.startsWith("io/cucumber/") && name.endsWith(".class")) {
                    cucumber[0] = true;
                    if (EXCLUDED_PACKAGES.stream().anyMatch(name::startsWith)) {
                        excluded[0] = true;
                    }
                }
            });
            return cucumber[0] && !excluded[0];
        } catch (final IOException e) {
            LOGGER.log(Level.FINE, e, () -> "Can't read " + file);
            return false;
        }
    }
}
