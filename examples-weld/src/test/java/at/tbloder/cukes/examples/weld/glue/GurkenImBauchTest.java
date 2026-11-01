package at.tbloder.cukes.examples.weld.glue;

import at.tbloder.cukes.CucumberOptions;
import at.tbloder.cukes.CukesTest;
import at.tbloder.cukes.examples.weld.Belly;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * German feature, steps in a separate glue class which gets the CDI bean injected.
 */
@CucumberOptions(features = "classpath:features/gurken-im-bauch.feature", glue = "at.tbloder.cukes.examples.weld.glue.steps")
class GurkenImBauchTest extends CukesTest {

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Belly.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
