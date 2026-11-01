package com.github.cukespace.api.event;

import java.net.URI;

/**
 * Fired after a Gherkin step was executed (or skipped).
 */
public final class AfterStep extends StepEvent {
    private final String status;
    private final Throwable error;

    public AfterStep(final String scenarioName, final URI featureUri, final int scenarioLine,
                     final String keyword, final String text, final int stepLine,
                     final String status, final Throwable error) {
        super(scenarioName, featureUri, scenarioLine, keyword, text, stepLine);
        this.status = status;
        this.error = error;
    }

    /**
     * @return the Cucumber status, e.g. {@code PASSED}, {@code FAILED}, {@code SKIPPED}
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the failure of the step or {@code null}
     */
    public Throwable getError() {
        return error;
    }
}
