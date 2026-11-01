package com.github.cukespace.client;

import com.github.cukespace.client.fixture.FixtureFeatures;
import com.github.cukespace.client.fixture.steps.FixtureBase;
import com.github.cukespace.client.fixture.steps.FixtureSteps;
import com.github.cukespace.client.fixture.steps.NotGlue;
import com.github.cukespace.shared.DeploymentIndex;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CukeSpaceArchiveProcessorTest {

    @Test
    void preparesWebArchive() throws Exception {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war").addClass(FixtureFeatures.class);

        new CukeSpaceArchiveProcessor().process(war, new TestClass(FixtureFeatures.class));

        final Set<String> libs = war.getContent(p -> p.get().startsWith("/WEB-INF/lib/")).keySet().stream()
                .map(ArchivePath::get).map(p -> p.substring("/WEB-INF/lib/".length())).collect(Collectors.toSet());
        assertTrue(libs.contains("cukespace-resources.jar"), libs::toString);
        assertTrue(libs.contains("cukespace-core.jar"), libs::toString);
        assertTrue(libs.stream().anyMatch(l -> l.startsWith("cucumber-core-")), libs::toString);
        assertTrue(libs.stream().anyMatch(l -> l.startsWith("cucumber-java-")), libs::toString);
        assertTrue(libs.stream().anyMatch(l -> l.startsWith("gherkin-")), libs::toString);
        assertTrue(libs.stream().noneMatch(l -> l.startsWith("cucumber-junit")), libs::toString);

        // glue the deployment did not contain, with its super class; non glue classes stay out
        assertTrue(war.contains("/WEB-INF/classes/" + path(FixtureSteps.class)));
        assertTrue(war.contains("/WEB-INF/classes/" + path(FixtureBase.class)));
        assertFalse(war.contains("/WEB-INF/classes/" + path(NotGlue.class)));

        final JavaArchive core = library(war, "cukespace-core.jar");
        assertTrue(core.contains("/META-INF/services/" + RemoteLoadableExtension.class.getName()));
        assertTrue(core.contains("/com/github/cukespace/container/CukeSpaceRunner.class"));
        assertFalse(core.contains("/com/github/cukespace/client/CukeSpaceArchiveProcessor.class"));

        final DeploymentIndex index = index(library(war, "cukespace-resources.jar"));
        assertEquals(1, index.getFeatures().size());
        final DeploymentIndex.FeatureEntry feature = index.getFeatures().get(0);
        assertEquals(URI.create("classpath:fixture/fixture.feature"), feature.getUri());
        assertEquals(List.of(FixtureFeatures.class.getName(), FixtureSteps.class.getName()),
                index.getGlueClasses().stream().sorted().toList());
        try (InputStream source = library(war, "cukespace-resources.jar").get(feature.getPath()).getAsset().openStream()) {
            assertTrue(new String(source.readAllBytes(), StandardCharsets.UTF_8).contains("Ünïcödé"));
        }
    }

    @Test
    void leavesJavaArchiveAlone() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addClass(FixtureFeatures.class);
        final int before = jar.getContent().size();

        new CukeSpaceArchiveProcessor().process(jar, new TestClass(FixtureFeatures.class));

        assertEquals(before, jar.getContent().size());
    }

    @Test
    void ignoresOtherTests() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class);

        new CukeSpaceArchiveProcessor().process(war, new TestClass(CukeSpaceArchiveProcessorTest.class));

        assertTrue(war.getContent().isEmpty());
    }

    private static String path(final Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    private static JavaArchive library(final Archive<?> war, final String name) {
        return (JavaArchive) ((ArchiveAsset) war.get("/WEB-INF/lib/" + name).getAsset()).getArchive();
    }

    private static DeploymentIndex index(final JavaArchive resources) throws IOException {
        final Path dir = Files.createTempDirectory("cukespace");
        final Path jar = dir.resolve("resources.jar");
        resources.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(jar.toFile());
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, null)) {
            return DeploymentIndex.load(loader).orElseThrow();
        } finally {
            Files.deleteIfExists(jar);
            Files.deleteIfExists(dir);
        }
    }
}
