package com.github.cukespace.examples.weld.glue.steps;

import com.github.cukespace.examples.weld.Belly;
import io.cucumber.java.Before;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GurkenSteps {

    @Inject
    Belly belly;

    @Before
    public void leererBauch() {
        belly.empty();
    }

    @Angenommen("ich habe {int} Gurken im Bauch")
    public void habeGurken(final int gurken) {
        belly.eat(gurken);
    }

    @Wenn("ich {int} weitere Gurken esse")
    public void esse(final int gurken) {
        belly.eat(gurken);
    }

    @Dann("habe ich {int} Gurken im Bauch")
    public void pruefe(final int gurken) {
        assertEquals(gurken, belly.getCukes());
    }
}
