package at.tbloder.cukes.examples.weld;

import jakarta.enterprise.context.Dependent;

/**
 * A plain CDI bean, injected into the test class and into a glue class.
 */
@Dependent
public class ExampleClassA {
    @Override
    public String toString() {
        return "ExampleClassA";
    }
}
