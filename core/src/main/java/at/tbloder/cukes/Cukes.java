package at.tbloder.cukes;

import at.tbloder.cukes.container.CukesRunner;

/**
 * Entry point to run the Cucumber features of a test instance.
 */
public final class Cukes {

    private Cukes() {
    }

    /**
     * Runs all features configured for the class of {@code testInstance}
     * (see {@link CucumberOptions}). The test instance itself is used for the
     * step definitions it declares.
     *
     * @param testInstance the current (Arquillian enriched) test instance
     * @throws AssertionError if at least one scenario did not pass
     */
    public static void runFeatures(final Object testInstance) {
        new CukesRunner(testInstance).run();
    }

    /**
     * @param testClass a test class
     * @return {@code true} when Cukes in the Belt handles the deployment of this class
     */
    public static boolean isCukesTest(final Class<?> testClass) {
        return CukesTest.class.isAssignableFrom(testClass) || testClass.isAnnotationPresent(CucumberOptions.class);
    }
}
