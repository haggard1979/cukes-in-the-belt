package at.tbloder.cukes.examples.tomee.client;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.tomee.Belly;
import at.tbloder.cukes.examples.tomee.BellyServlet;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs on the client and talks to the deployed application over HTTP.
 */
@RunAsClient
@CucumberOptions(features = "classpath:features/client.feature")
class BellyOverHttpTest extends CukesTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "http.war")
                .addClasses(Belly.class, BellyServlet.class);
    }

    @ArquillianResource
    URL base;

    private final HttpClient http = HttpClient.newHttpClient();

    @Given("the belly endpoint is empty")
    public void empty() throws Exception {
        send(HttpRequest.newBuilder(belly("")).DELETE());
    }

    @When("I post {int} cukes to the belly endpoint")
    public void post(final int cukes) throws Exception {
        send(HttpRequest.newBuilder(belly("?eat=" + cukes)).POST(HttpRequest.BodyPublishers.noBody()));
    }

    @Then("the belly endpoint reports {int} cukes")
    public void reports(final int cukes) throws Exception {
        assertEquals(String.valueOf(cukes), send(HttpRequest.newBuilder(belly("")).GET()));
    }

    private URI belly(final String query) {
        return URI.create(base.toExternalForm() + "belly" + query);
    }

    private String send(final HttpRequest.Builder request) throws IOException, InterruptedException {
        final HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        return response.body();
    }
}
