package at.tbloder.cukes.api.event;

import java.net.URI;

/**
 * Fired after a scenario (including the Cucumber {@code @After} hooks) finished.
 */
public final class AfterScenario extends ScenarioEvent {
    private final String status;
    private final Throwable error;

    public AfterScenario(final String scenarioName, final URI featureUri, final int scenarioLine,
                         final String status, final Throwable error) {
        super(scenarioName, featureUri, scenarioLine);
        this.status = status;
        this.error = error;
    }

    /**
     * @return the Cucumber status, e.g. {@code PASSED}, {@code FAILED}, {@code UNDEFINED}
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the failure of the scenario or {@code null}
     */
    public Throwable getError() {
        return error;
    }
}
