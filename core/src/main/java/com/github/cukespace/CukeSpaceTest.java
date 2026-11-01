package com.github.cukespace;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for Cucumber features running inside an Arquillian deployment.
 *
 * <pre>{@code
 * @CucumberOptions(features = "classpath:features/belly.feature")
 * class CukesInBellyTest extends CukeSpaceTest {
 *
 *     @Deployment
 *     static WebArchive deployment() {
 *         return ShrinkWrap.create(WebArchive.class).addClass(Belly.class);
 *     }
 *
 *     @Inject
 *     Belly belly;
 *
 *     @Given("I have {int} cukes in my belly")
 *     public void eat(int cukes) {
 *         belly.eat(cukes);
 *     }
 * }
 * }</pre>
 *
 * The features run in the same place as any other Arquillian test method: in the
 * container, or on the client for {@code @RunAsClient} / {@code @Deployment(testable = false)}.
 * If extending this class is not possible, annotate the test with
 * {@code @ExtendWith(ArquillianExtension.class)} and call
 * {@link CukeSpace#runFeatures(Object)} from a {@code @Test} method.
 */
@ExtendWith(ArquillianExtension.class)
public abstract class CukeSpaceTest {

    @Test
    @DisplayName("Cucumber features")
    public void cukespaceRunFeatures() throws Throwable {
        CukeSpace.runFeatures(this);
    }
}
