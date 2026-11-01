package at.tbloder.cukes.api.event;

import java.net.URI;

/**
 * Fired before a Gherkin step is executed.
 */
public final class BeforeStep extends StepEvent {
    public BeforeStep(final String scenarioName, final URI featureUri, final int scenarioLine,
                      final String keyword, final String text, final int stepLine) {
        super(scenarioName, featureUri, scenarioLine, keyword, text, stepLine);
    }
}
