package com.github.cukespace.examples.tomee;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Belly {
    private int cukes;

    public synchronized void eat(final int cukes) {
        this.cukes += cukes;
    }

    public synchronized void empty() {
        cukes = 0;
    }

    public synchronized int getCukes() {
        return cukes;
    }
}
