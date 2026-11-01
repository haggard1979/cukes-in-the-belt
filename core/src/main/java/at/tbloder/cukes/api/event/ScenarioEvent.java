package at.tbloder.cukes.api.event;

import java.net.URI;

/**
 * Base class of the Arquillian events Cukes in the Belt fires while a scenario runs.
 * Other Arquillian extensions can observe them, e.g.
 * {@code public void screenshot(@Observes AfterStep step)}.
 */
public abstract class ScenarioEvent {
    private final String scenarioName;
    private final URI featureUri;
    private final int scenarioLine;

    protected ScenarioEvent(final String scenarioName, final URI featureUri, final int scenarioLine) {
        this.scenarioName = scenarioName;
        this.featureUri = featureUri;
        this.scenarioLine = scenarioLine;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public URI getFeatureUri() {
        return featureUri;
    }

    public int getScenarioLine() {
        return scenarioLine;
    }
}
