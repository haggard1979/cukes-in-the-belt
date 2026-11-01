package at.tbloder.cukes.examples.weld.example.commons;

import at.tbloder.cukes.examples.weld.ExampleClassA;
import io.cucumber.java.en.Given;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A step shared by several features: found because its package is below the
 * package of the test class (default glue).
 */
public class CommonStep {

    @Inject
    ExampleClassA exampleClassA;

    @Given("^toString$")
    public void callToString() {
        assertEquals("ExampleClassA", exampleClassA.toString(), "@Inject in the glue class");
    }
}
