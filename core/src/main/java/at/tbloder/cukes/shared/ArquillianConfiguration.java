package at.tbloder.cukes.shared;

import java.util.Map;

/**
 * Holds the properties of the {@code <extension qualifier="cucumber">} section
 * of {@code arquillian.xml} for code running in the client JVM (embedded
 * containers, {@code @RunAsClient}).
 */
public final class ArquillianConfiguration {
    private static volatile Map<String, String> properties = Map.of();

    private ArquillianConfiguration() {
    }

    public static Map<String, String> properties() {
        return properties;
    }

    public static void properties(final Map<String, String> newProperties) {
        properties = Map.copyOf(newProperties);
    }
}
