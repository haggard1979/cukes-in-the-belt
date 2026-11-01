package at.tbloder.cukes.client.fixture;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import io.cucumber.java.en.Given;

@CucumberOptions(features = "classpath:fixture/fixture.feature", extraGlue = "at.tbloder.cukes.client.fixture.steps")
public class FixtureFeatures extends CukesTest {
    @Given("a fixture step")
    public void step() {
        // no-op
    }
}
