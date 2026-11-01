package at.tbloder.cukes.examples.wildfly.glue;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.wildfly.Belly;
import at.tbloder.cukes.examples.wildfly.CukeService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * German feature; the glue class is not part of the deployment, Cukes in the Belt adds it.
 * WildFly injects {@code @EJB} and {@code @Resource} into the glue instance created through CDI.
 */
@CucumberOptions(features = "classpath:features/gurken-im-bauch.feature", glue = "at.tbloder.cukes.examples.wildfly.glue.steps")
class GurkenImBauchTest extends CukesTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "gurken.war")
                .addClasses(Belly.class, CukeService.class);
    }
}
