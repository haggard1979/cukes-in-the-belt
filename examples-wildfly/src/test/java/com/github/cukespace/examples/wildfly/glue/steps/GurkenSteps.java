package com.github.cukespace.examples.wildfly.glue.steps;

import com.github.cukespace.examples.wildfly.Belly;
import com.github.cukespace.examples.wildfly.CukeService;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GurkenSteps {

    @Inject
    Belly bauch;

    @EJB
    CukeService service;

    // the default data source of WildFly's standalone.xml
    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    DataSource datenquelle;

    private boolean erzeugt;

    @PostConstruct
    void erzeugt() {
        erzeugt = true;
    }

    @Angenommen("mein Bauch ist leer")
    public void leererBauch() {
        assertTrue(erzeugt, "@PostConstruct not called");
        bauch.empty();
    }

    @Wenn("ich {int} Gurken esse")
    public void esse(final int gurken) {
        assertNotNull(service, "@EJB not injected");
        service.eat(gurken);
    }

    @Dann("sind {int} Gurken in meinem Bauch")
    public void pruefe(final int gurken) {
        assertEquals(gurken, bauch.getCukes());
    }

    @Dann("die Datenquelle ist verfügbar")
    public void datenquelle() throws SQLException {
        assertNotNull(datenquelle, "@Resource not injected");
        try (Connection connection = datenquelle.getConnection()) {
            assertTrue(connection.isValid(5));
        }
    }
}
