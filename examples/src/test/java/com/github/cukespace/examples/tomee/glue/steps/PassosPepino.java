package com.github.cukespace.examples.tomee.glue.steps;

import com.github.cukespace.examples.tomee.Belly;
import com.github.cukespace.examples.tomee.CukeService;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PassosPepino {

    @Inject
    Belly barriga;

    // session beans are CDI beans: @Inject works in every server (@EJB in glue classes depends on the server)
    @Inject
    CukeService servico;

    private boolean criado;

    @PostConstruct
    void criar() {
        criado = true; // glue classes are created by CDI: lifecycle callbacks work
    }

    @Dado("que a barriga está vazia")
    public void barrigaVazia() {
        assertTrue(criado, "@PostConstruct not called");
        barriga.empty();
    }

    @Quando("eu como {int} pepinos")
    public void como(final int pepinos) {
        servico.eat(pepinos);
    }

    @Então("a barriga contém {int} pepinos")
    public void contem(final int pepinos) {
        assertEquals(pepinos, barriga.getCukes());
    }
}
