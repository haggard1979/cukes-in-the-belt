package com.github.cukespace.api.event;

import java.net.URI;

/**
 * Fired before the first step (and before the Cucumber {@code @Before} hooks) of a scenario.
 */
public final class BeforeScenario extends ScenarioEvent {
    public BeforeScenario(final String scenarioName, final URI featureUri, final int scenarioLine) {
        super(scenarioName, featureUri, scenarioLine);
    }
}
