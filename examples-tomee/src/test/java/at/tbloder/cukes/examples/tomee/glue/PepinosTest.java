package at.tbloder.cukes.examples.tomee.glue;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.tomee.Belly;
import at.tbloder.cukes.examples.tomee.CukeService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Portuguese feature; the glue class is not part of the deployment, Cukes in the Belt adds it.
 */
@CucumberOptions(features = "classpath:features/pepinos.feature", glue = "at.tbloder.cukes.examples.tomee.glue.steps")
class PepinosTest extends CukesTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "pepinos.war")
                .addClasses(Belly.class, CukeService.class);
    }
}
