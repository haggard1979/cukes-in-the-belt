package at.tbloder.cukes.shared;

import at.tbloder.cukes.CucumberOptions;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.snippets.SnippetType;

import io.cucumber.core.options.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the Cucumber {@link RuntimeOptions} of a test class. Used on the client
 * (to find features and glue) and in the container (to run them), so both sides
 * agree on the configuration.
 */
public final class CukesOptions {
    public static final String PLUGIN_PROPERTY = Constants.PLUGIN_PROPERTY_NAME;

    private static final List<String> PROPERTY_NAMES = propertyNames();

    private CukesOptions() {
    }

    /**
     * Returns the Cucumber properties: {@code arquillian.xml} overridden by {@code cucumber.properties}, environment variables and system properties (in this order).
     *
     * @param arquillianProperties the properties of the {@code cucumber} extension in {@code arquillian.xml}
     * @return the Cucumber properties: {@code arquillian.xml} overridden by {@code cucumber.properties},
     *         environment variables and system properties (in this order)
     */
    public static Map<String, String> collectProperties(final Map<String, String> arquillianProperties) {
        final Map<String, String> properties = new LinkedHashMap<>();
        arquillianProperties.forEach((key, value) -> {
            if (key.startsWith("cucumber.")) {
                properties.put(key, value);
            }
        });
        // only the known keys: the map also resolves environment variables like CUCUMBER_FILTER_TAGS
        final Map<String, String> cucumberProperties = CucumberProperties.create();
        for (final String key : PROPERTY_NAMES) {
            final String value = cucumberProperties.get(key);
            if (value != null) {
                properties.put(key, value);
            }
        }
        return properties;
    }

    private static List<String> propertyNames() {
        final List<String> names = new ArrayList<>();
        for (final Field field : Constants.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class
                    && field.getName().endsWith("_PROPERTY_NAME")) {
                try {
                    names.add((String) field.get(null));
                } catch (final IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return List.copyOf(names);
    }

    /**
     * Returns the runtime options.
     *
     * @param testClass  the test class, possibly annotated with {@link CucumberOptions}
     * @param properties Cucumber properties overriding the annotation
     * @return the runtime options
     */
    public static RuntimeOptions create(final Class<?> testClass, final Map<String, String> properties) {
        final RuntimeOptionsBuilder annotationOptions = new CucumberOptionsAnnotationParser()
                .withOptionsProvider(CukesOptions::options)
                .parse(testClass);
        // steps declared in the test class are always available
        annotationOptions.addGlueClass(testClass.getName());
        if (!hasPlugin(testClass) && !properties.containsKey(PLUGIN_PROPERTY)) {
            annotationOptions.addPluginName("pretty");
        }
        return new CucumberPropertiesParser()
                .parse(properties)
                .build(annotationOptions.build());
    }

    private static boolean hasPlugin(final Class<?> testClass) {
        for (Class<?> c = testClass; c != null && c != Object.class; c = c.getSuperclass()) {
            final CucumberOptions options = c.getDeclaredAnnotation(CucumberOptions.class);
            if (options != null && options.plugin().length > 0) {
                return true;
            }
        }
        return false;
    }

    private static CucumberOptionsAnnotationParser.CucumberOptions options(final Class<?> clazz) {
        // declared only: the parser walks the class hierarchy itself
        final CucumberOptions annotation = clazz.getDeclaredAnnotation(CucumberOptions.class);
        return annotation == null ? null : new AnnotationAdapter(annotation);
    }

    private static final class AnnotationAdapter implements CucumberOptionsAnnotationParser.CucumberOptions {
        private final CucumberOptions annotation;

        private AnnotationAdapter(final CucumberOptions annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean dryRun() {
            return annotation.dryRun();
        }

        @Override
        public String[] features() {
            return annotation.features();
        }

        @Override
        public String[] glue() {
            return annotation.glue();
        }

        @Override
        public String[] extraGlue() {
            return annotation.extraGlue();
        }

        @Override
        public String tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
        }

        @Override
        public boolean publish() {
            return false;
        }

        @Override
        public boolean monochrome() {
            return annotation.monochrome();
        }

        @Override
        public String[] name() {
            return annotation.name();
        }

        @Override
        public SnippetType snippets() {
            return annotation.snippets();
        }

        @Override
        public Class<? extends ObjectFactory> objectFactory() {
            return annotation.objectFactory() == CucumberOptions.NoObjectFactory.class ? null : annotation.objectFactory();
        }

        @Override
        public Class<? extends UuidGenerator> uuidGenerator() {
            return null;
        }
    }
}
