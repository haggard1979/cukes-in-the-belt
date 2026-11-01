package com.github.cukespace.client.fixture;

import com.github.cukespace.CucumberOptions;
import com.github.cukespace.CukeSpaceTest;
import io.cucumber.java.en.Given;

@CucumberOptions(features = "classpath:fixture/fixture.feature", extraGlue = "com.github.cukespace.client.fixture.steps")
public class FixtureFeatures extends CukeSpaceTest {
    @Given("a fixture step")
    public void step() {
        // no-op
    }
}
