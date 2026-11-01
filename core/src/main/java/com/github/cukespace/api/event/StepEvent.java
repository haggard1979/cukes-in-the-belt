package com.github.cukespace.api.event;

import java.net.URI;

/**
 * Base class of the step related events.
 */
public abstract class StepEvent extends ScenarioEvent {
    private final String keyword;
    private final String text;
    private final int stepLine;

    protected StepEvent(final String scenarioName, final URI featureUri, final int scenarioLine,
                        final String keyword, final String text, final int stepLine) {
        super(scenarioName, featureUri, scenarioLine);
        this.keyword = keyword;
        this.text = text;
        this.stepLine = stepLine;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getText() {
        return text;
    }

    public int getStepLine() {
        return stepLine;
    }
}
