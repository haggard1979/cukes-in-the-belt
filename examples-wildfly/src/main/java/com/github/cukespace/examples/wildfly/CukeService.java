package com.github.cukespace.examples.wildfly;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

/**
 * An EJB, to show that step definitions can use {@code @EJB}.
 */
@Stateless
public class CukeService {
    @Inject
    private Belly belly;

    public int eat(final int cukes) {
        belly.eat(cukes);
        return belly.getCukes();
    }
}
