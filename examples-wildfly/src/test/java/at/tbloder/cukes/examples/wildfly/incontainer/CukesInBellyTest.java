package at.tbloder.cukes.examples.wildfly.incontainer;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.wildfly.Belly;
import at.tbloder.cukes.examples.wildfly.CukeService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs inside WildFly (separate JVM, servlet protocol): the steps of the test class use CDI and EJB injection.
 */
@CucumberOptions(features = "classpath:features/cukes-in-belly.feature")
class CukesInBellyTest extends CukesTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "belly.war")
                .addClasses(Belly.class, CukeService.class);
    }

    @Inject
    Belly belly;

    @EJB
    CukeService service;

    @Given("my belly is empty")
    public void emptyBelly() {
        belly.empty();
    }

    @Given("I have {int} cukes in my belly")
    public void haveCukes(final int cukes) {
        belly.eat(cukes);
    }

    @When("I eat {int} more cukes")
    public void eatMore(final int cukes) {
        service.eat(cukes);
    }

    @Then("my belly should contain {int} cukes")
    public void checkCukes(final int cukes) {
        assertEquals(cukes, belly.getCukes());
    }
}
