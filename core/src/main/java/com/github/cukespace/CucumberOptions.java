package com.github.cukespace;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.snippets.SnippetType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures how Cucumber runs the features of a CukeSpace test.
 * <p>
 * All values can be overridden by the usual Cucumber properties
 * ({@code cucumber.filter.tags}, {@code cucumber.plugin}, ...) taken from
 * {@code cucumber.properties}, environment variables, system properties or the
 * {@code <extension qualifier="cucumber">} section of {@code arquillian.xml}.
 * <p>
 * When no features are configured, the package of the test class is used
 * ({@code classpath:com/example}). The test class itself is always part of the
 * glue, so step definitions can be written directly in the test class.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CucumberOptions {

    /**
     * @return feature files or directories, e.g. {@code classpath:features/belly.feature}
     *         or {@code src/test/resources/features}. Line filters such as
     *         {@code classpath:features/belly.feature:12} are supported.
     */
    String[] features() default {};

    /**
     * @return packages to scan for step definitions and hooks, e.g. {@code com.example.steps}.
     *         Replaces the default glue (the package of the test class).
     */
    String[] glue() default {};

    /**
     * @return packages to scan for step definitions in addition to the
     *         package of the test class.
     */
    String[] extraGlue() default {};

    /**
     * @return a Cucumber tag expression, e.g. {@code "@smoke and not @wip"}.
     */
    String tags() default "";

    /**
     * @return Cucumber plugins, e.g. {@code "html:target/cucumber.html"}.
     *         Defaults to {@code pretty} when no plugin is configured at all.
     */
    String[] plugin() default {};

    /**
     * @return regular expressions; only scenarios whose name matches are run.
     */
    String[] name() default {};

    /**
     * @return {@code true} to check that every step has a definition without executing it.
     */
    boolean dryRun() default false;

    /**
     * @return {@code true} to disable colored console output.
     */
    boolean monochrome() default false;

    /**
     * @return the format of suggested snippets for undefined steps.
     */
    SnippetType snippets() default SnippetType.UNDERSCORE;

    /**
     * @return a custom Cucumber object factory. By default CukeSpace uses its own
     *         factory: the test instance serves the steps declared in the test class
     *         and all other glue classes are created per scenario and enriched by
     *         Arquillian ({@code @Inject}, {@code @EJB}, {@code @Resource},
     *         {@code @ArquillianResource}, ...).
     */
    Class<? extends ObjectFactory> objectFactory() default NoObjectFactory.class;

    /**
     * Marker for "no custom object factory".
     */
    abstract class NoObjectFactory implements ObjectFactory {
        private NoObjectFactory() {
        }
    }
}
