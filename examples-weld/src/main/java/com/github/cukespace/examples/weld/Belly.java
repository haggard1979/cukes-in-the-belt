package com.github.cukespace.examples.weld;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Belly {
    private int cukes;

    public void eat(final int cukes) {
        this.cukes += cukes;
    }

    public void empty() {
        cukes = 0;
    }

    public int getCukes() {
        return cukes;
    }
}
