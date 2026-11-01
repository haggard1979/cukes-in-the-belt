package com.github.cukespace.examples.tomee.glue;

import com.github.cukespace.CucumberOptions;
import com.github.cukespace.CukeSpaceTest;
import com.github.cukespace.examples.tomee.Belly;
import com.github.cukespace.examples.tomee.CukeService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Portuguese feature; the glue class is not part of the deployment, CukeSpace adds it.
 */
@CucumberOptions(features = "classpath:features/pepinos.feature", glue = "com.github.cukespace.examples.tomee.glue.steps")
class PepinosTest extends CukeSpaceTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "pepinos.war")
                .addClasses(Belly.class, CukeService.class);
    }
}
