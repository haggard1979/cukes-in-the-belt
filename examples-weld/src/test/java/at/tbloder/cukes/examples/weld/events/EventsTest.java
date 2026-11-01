package at.tbloder.cukes.examples.weld.events;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.Cukes;
import io.cucumber.java.en.Given;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Scenarios and steps are published as Arquillian events.
 */
@ExtendWith(ArquillianExtension.class)
@CucumberOptions(features = "classpath:features/events.feature")
class EventsTest {

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Given("a step")
    public void step() {
        StepRecorder.EVENTS.add("step executed");
    }

    @Test
    void eventsArePublished() {
        StepRecorder.EVENTS.clear();
        Cukes.runFeatures(this);
        assertEquals(List.of(
                "before scenario One step",
                "before step Given a step",
                "step executed",
                "after step a step PASSED",
                "after scenario One step PASSED"), StepRecorder.EVENTS);
    }
}
