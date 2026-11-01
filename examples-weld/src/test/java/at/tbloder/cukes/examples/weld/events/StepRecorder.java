package at.tbloder.cukes.examples.weld.events;

import at.tbloder.cukes.api.event.AfterScenario;
import at.tbloder.cukes.api.event.AfterStep;
import at.tbloder.cukes.api.event.BeforeScenario;
import at.tbloder.cukes.api.event.BeforeStep;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An Arquillian extension observing the Cukes in the Belt events (e.g. to take a screenshot after each step).
 */
public class StepRecorder {
    static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    public void beforeScenario(@Observes final BeforeScenario event) {
        EVENTS.add("before scenario " + event.getScenarioName());
    }

    public void beforeStep(@Observes final BeforeStep event) {
        EVENTS.add("before step " + event.getKeyword() + event.getText());
    }

    public void afterStep(@Observes final AfterStep event) {
        EVENTS.add("after step " + event.getText() + " " + event.getStatus());
    }

    public void afterScenario(@Observes final AfterScenario event) {
        EVENTS.add("after scenario " + event.getScenarioName() + " " + event.getStatus());
    }

    public static class Extension implements LoadableExtension {
        @Override
        public void register(final ExtensionBuilder builder) {
            builder.observer(StepRecorder.class);
        }
    }
}
