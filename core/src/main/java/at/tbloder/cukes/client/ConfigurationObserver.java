package at.tbloder.cukes.client;

import at.tbloder.cukes.shared.ArquillianConfiguration;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads {@code <extension qualifier="cucumber">} from {@code arquillian.xml}.
 * Its properties are Cucumber properties, e.g. {@code cucumber.filter.tags}.
 */
public class ConfigurationObserver {
    public static final String QUALIFIER = "cucumber";

    public void configure(@Observes final ArquillianDescriptor descriptor) {
        final Map<String, String> properties = new LinkedHashMap<>();
        for (final ExtensionDef extension : descriptor.getExtensions()) {
            if (QUALIFIER.equals(extension.getExtensionName())) {
                properties.putAll(extension.getExtensionProperties());
            }
        }
        ArquillianConfiguration.properties(properties);
    }
}
