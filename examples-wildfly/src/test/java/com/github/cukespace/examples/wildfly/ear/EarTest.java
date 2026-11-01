package com.github.cukespace.examples.wildfly.ear;

import com.github.cukespace.CucumberOptions;
import com.github.cukespace.CukeSpaceTest;
import com.github.cukespace.examples.wildfly.Belly;
import com.github.cukespace.examples.wildfly.CukeService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An enterprise archive: the EJB module holds the business code, the web module the test.
 * CukeSpace adds its libraries to the module containing the test class.
 */
@CucumberOptions(features = "classpath:features/cukes-in-belly.feature")
class EarTest extends CukeSpaceTest {

    @Deployment
    static EnterpriseArchive deployment() {
        final JavaArchive ejb = ShrinkWrap.create(JavaArchive.class, "belly-ejb.jar")
                .addClasses(Belly.class, CukeService.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        final WebArchive web = ShrinkWrap.create(WebArchive.class, "belly-web.war")
                .addClass(EarTest.class);
        return ShrinkWrap.create(EnterpriseArchive.class, "belly.ear")
                .addAsModules(ejb, web);
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
        service.eat(cukes);
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
