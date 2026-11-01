package at.tbloder.cukes.examples.weld.intest;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.weld.Belly;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Steps declared directly in the test class, using CDI injection.
 */
@CucumberOptions(features = "classpath:features/cukes-in-belly.feature", tags = "not @ignored")
class CukesInBellyTest extends CukesTest {

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

    @When("I eat {int} more cukes")
    public void eatMore(final int cukes) {
        belly.eat(cukes);
    }

    @Then("my belly should contain {int} cukes")
    public void checkCukes(final int cukes) {
        assertEquals(cukes, belly.getCukes());
    }
}
