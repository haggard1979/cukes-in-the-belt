package com.github.cukespace.examples.weld.failure;

import com.github.cukespace.CucumberOptions;
import com.github.cukespace.CukeSpace;
import com.github.cukespace.ScenarioFailedError;
import com.github.cukespace.examples.weld.Belly;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.MultipleFailuresError;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the features without extending CukeSpaceTest and checks how failures are reported.
 */
@ExtendWith(ArquillianExtension.class)
@CucumberOptions(features = "classpath:features/failing.feature")
class FailureReportingTest {

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Belly.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    Belly belly;

    @Before
    public void emptyBelly() {
        belly.empty();
    }

    @Given("I have {int} cukes in my belly")
    public void haveCukes(final int cukes) {
        belly.eat(cukes);
    }

    @Then("my belly should contain {int} cukes")
    public void checkCukes(final int cukes) {
        assertEquals(cukes, belly.getCukes());
    }

    @Test
    void failedAndUndefinedScenariosAreReported() {
        final MultipleFailuresError error = assertThrows(MultipleFailuresError.class, () -> CukeSpace.runFeatures(this));
        final String message = error.getMessage();
        assertAll(
                () -> assertEquals(2, error.getFailures().size(), message),
                () -> assertTrue(error.getFailures().stream().allMatch(ScenarioFailedError.class::isInstance), message),
                () -> assertTrue(message.startsWith("2 of 3 scenarios failed"), message),
                () -> assertTrue(message.contains("A failing step"), message),
                () -> assertTrue(message.contains("An undefined step"), message),
                () -> assertTrue(message.contains("I drink a glass of water"), message),
                () -> assertTrue(message.contains("@When(\"I drink a glass of water\")"), message));
    }
}
