package com.github.cukespace.examples.weld.glue;

import com.github.cukespace.CucumberOptions;
import com.github.cukespace.CukeSpaceTest;
import com.github.cukespace.examples.weld.Belly;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * German feature, steps in a separate glue class which gets the CDI bean injected.
 */
@CucumberOptions(features = "classpath:features/gurken-im-bauch.feature", glue = "com.github.cukespace.examples.weld.glue.steps")
class GurkenImBauchTest extends CukeSpaceTest {

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Belly.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
