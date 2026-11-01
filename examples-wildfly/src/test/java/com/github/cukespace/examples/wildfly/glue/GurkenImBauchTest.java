package com.github.cukespace.examples.wildfly.glue;

import com.github.cukespace.CucumberOptions;
import com.github.cukespace.CukeSpaceTest;
import com.github.cukespace.examples.wildfly.Belly;
import com.github.cukespace.examples.wildfly.CukeService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * German feature; the glue class is not part of the deployment, CukeSpace adds it.
 * WildFly injects {@code @EJB} and {@code @Resource} into the glue instance created through CDI.
 */
@CucumberOptions(features = "classpath:features/gurken-im-bauch.feature", glue = "com.github.cukespace.examples.wildfly.glue.steps")
class GurkenImBauchTest extends CukeSpaceTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "gurken.war")
                .addClasses(Belly.class, CukeService.class);
    }
}
