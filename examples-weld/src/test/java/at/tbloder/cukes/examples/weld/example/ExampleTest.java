package at.tbloder.cukes.examples.weld.example;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.weld.ExampleClassA;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Taken over from the former cukespace-arquillian-example project:
 * <ul>
 * <li>German keywords, English step texts and regular expressions in the step annotations,</li>
 * <li>steps in the test class combined with a glue class of a sub package ({@code commons}),
 * found through the default glue (the package of the test class) without configuring it.</li>
 * </ul>
 */
@CucumberOptions(features = "classpath:features/example.feature")
class ExampleTest extends CukesTest {

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(ExampleClassA.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    ExampleClassA exampleClassA;

    int x;

    @Given("^a variable x with value (.+)$")
    public void givenXValue(final int value) {
        assertNotNull(exampleClassA, "@Inject in the test class");
        x = value;
    }

    @When("^I multiply x by (.+)$")
    public void whenIMultiplyXBy(final int value) {
        x = x * value;
    }

    @Then("^x should equal (.+)$")
    public void thenXShouldBe(final int value) {
        assertEquals(value, x);
    }
}
