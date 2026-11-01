package com.github.cukespace.container;

import com.github.cukespace.shared.DeploymentIndex;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;
import io.cucumber.core.runtime.FeatureSupplier;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Supplies the features the client copied into the deployment.
 */
final class IndexedFeatureSupplier implements FeatureSupplier {
    private final ClassLoader loader;
    private final List<DeploymentIndex.FeatureEntry> entries;

    IndexedFeatureSupplier(final ClassLoader loader, final List<DeploymentIndex.FeatureEntry> entries) {
        this.loader = loader;
        this.entries = entries;
    }

    @Override
    public List<Feature> get() {
        final FeatureParser parser = new FeatureParser(UUID::randomUUID);
        final List<Feature> features = new ArrayList<>(entries.size());
        for (final DeploymentIndex.FeatureEntry entry : entries) {
            parser.parseResource(new Resource() {
                @Override
                public URI getUri() {
                    return entry.getUri();
                }

                @Override
                public InputStream getInputStream() throws FileNotFoundException {
                    final InputStream stream = loader.getResourceAsStream(entry.getPath());
                    if (stream == null) {
                        throw new FileNotFoundException(entry.getPath() + " (" + entry.getUri() + ") is missing in the deployment");
                    }
                    return stream;
                }
            }).ifPresent(features::add);
        }
        return features;
    }
}
