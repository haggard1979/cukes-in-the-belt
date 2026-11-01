package com.github.cukespace;

import com.github.cukespace.container.CukeSpaceRunner;

/**
 * Entry point to run the Cucumber features of a test instance.
 */
public final class CukeSpace {

    private CukeSpace() {
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
        new CukeSpaceRunner(testInstance).run();
    }

    /**
     * @param testClass a test class
     * @return {@code true} when CukeSpace handles the deployment of this class
     */
    public static boolean isCukeSpaceTest(final Class<?> testClass) {
        return CukeSpaceTest.class.isAssignableFrom(testClass) || testClass.isAnnotationPresent(CucumberOptions.class);
    }
}
