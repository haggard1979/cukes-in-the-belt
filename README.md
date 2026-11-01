Cukes in Space!
===============

Cukes in Space! runs [Cucumber][] features inside the Jakarta EE container of
your choice using [Arquillian][] and JUnit 5. Step definitions are served by
the container: they can use `@Inject`, `@EJB`, `@Resource`,
`@ArquillianResource`, ...

Version 2 requires:

| | Version |
|---|---|
| Java | 17+ |
| Cucumber-JVM | 8.x (`io.cucumber`) |
| JUnit | 5 (Jupiter) |
| Arquillian | 1.10+ (`arquillian-junit5-container`) |
| Jakarta EE | 10+ (`jakarta.*`) |

JUnit 4, TestNG and `javax.*` are supported by the 1.x versions only, see
[Migrating from 1.x](#migrating-from-1x).

[Arquillian]: https://arquillian.org/
[Cucumber]: https://cucumber.io/docs/installation/java/

## Installation

```bash
mvn install
```

```xml
<dependency>
    <groupId>com.github.cukespace</groupId>
    <artifactId>cukespace-core</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

`cukespace-core` brings Cucumber (`cucumber-core`, `cucumber-java`), JUnit 5
and `arquillian-junit5-container`. Add the Arquillian adapter of your
container (WildFly, TomEE, Payara, Weld embedded, ...) as usual.

## Writing features

Extend `CukeSpaceTest`, create the deployment and write the steps directly in
the test class:

```java
@CucumberOptions(features = "classpath:features/cukes-in-belly.feature")
class CukesInBellyTest extends CukeSpaceTest {

    @Deployment
    static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Belly.class, CukeService.class);
    }

    @Inject
    Belly belly;

    @EJB
    CukeService service;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int cukes) {
        belly.eat(cukes);
    }

    @When("I eat {int} more cukes")
    public void eatMore(int cukes) {
        service.eat(cukes);
    }

    @Then("my belly should contain {int} cukes")
    public void checkCukes(int cukes) {
        assertEquals(cukes, belly.getCukes());
    }
}
```

`CukeSpaceTest` contributes one JUnit test, `Cucumber features`, which runs
all scenarios. Arquillian executes it like any other test method: inside the
container, or on the client with `@RunAsClient` / `@Deployment(testable = false)`.
When some scenarios do not pass, the test fails with a `MultipleFailuresError`
listing every failed scenario (`ScenarioFailedError`) and the snippets for
undefined steps.

If the test can't extend `CukeSpaceTest`, run the features yourself:

```java
@ExtendWith(ArquillianExtension.class)
@CucumberOptions(features = "classpath:features/belly.feature")
class BellyTest {
    @Test
    void features() {
        CukeSpace.runFeatures(this);
    }
}
```

### Glue classes

Steps and hooks are looked up in

* the test class (always),
* the package of the test class, or the packages given in
  `@CucumberOptions(glue = ...)` (replaces the default) or `extraGlue = ...`
  (in addition to the default).

Glue classes other than the test class are created once per scenario. When
CDI is available they are created by the container as non-contextual
instances (`@Inject`, `@PostConstruct`, `@PreDestroy` and whatever else the
container injects into such instances), then Arquillian enriches them
(`@ArquillianResource`, ...). Without CDI they need a no-argument constructor.

Prefer `@Inject` for session beans in glue classes: whether `@EJB` and
`@Resource` are injected into non-contextual instances depends on the server
(TomEE, for instance, does not). In the test class itself they always work.

Glue classes don't have to be added to the deployment: CukeSpace adds the ones
the deployment lacks.

### Configuration

`@CucumberOptions` (`com.github.cukespace.CucumberOptions`) supports
`features`, `glue`, `extraGlue`, `tags`, `plugin`, `name`, `dryRun`,
`monochrome`, `snippets` and `objectFactory`. Without features, the package
of the test class is used (`classpath:com/example`). Without plugins,
`pretty` is used.

Every option can be overridden with the usual [Cucumber properties][properties]
(`cucumber.filter.tags`, `cucumber.plugin`, `cucumber.features`, ...). They are
read from, by increasing priority:

1. the `cucumber` extension in `arquillian.xml`,
2. `cucumber.properties`,
3. environment variables (`CUCUMBER_FILTER_TAGS`),
4. system properties (`-Dcucumber.filter.tags=@smoke`).

```xml
<extension qualifier="cucumber">
    <property name="cucumber.filter.tags">not @wip</property>
    <property name="cucumber.plugin">pretty, html:target/cucumber.html</property>
</extension>
```

The properties are evaluated on the client and shipped with the deployment,
so they also apply to remote containers. File based plugins (`html:`,
`json:`, `junit:`, ...) write on the machine running the features: with a
remote server, use absolute paths or run the reports on the client.

[properties]: https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-core#properties-environment-variables-system-options

### Deployments

For a `WebArchive` or an `EnterpriseArchive`, CukeSpace adds to the archive
holding the test class:

* the feature files, copied by the client (no classpath scanning in the
  server, which is unreliable with VFS and nested archives),
* the list of glue classes and the missing glue classes,
* the Cucumber jars and the container side of CukeSpace.

A `JavaArchive` deployment is left as it is: it is used by embedded containers
such as Weld embedded, which run with the class path of the test.

### Arquillian events

Other Arquillian extensions can observe what Cucumber does, e.g. to take a
screenshot after each step:

```java
public class Screenshots {
    public void afterStep(@Observes AfterStep step) {
        if ("FAILED".equals(step.getStatus())) {
            // ...
        }
    }
}
```

Events (package `com.github.cukespace.api.event`): `BeforeScenario`,
`AfterScenario`, `BeforeStep`, `AfterStep`.

## Examples

* `examples`: TomEE 10 embedded, `WebArchive` deployments: steps in the test
  class using CDI and EJB, a Portuguese feature with separate glue, a
  `@RunAsClient` test calling a servlet over HTTP, tags configured in
  `arquillian.xml`.
* `examples-weld`: Weld embedded, `JavaArchive` deployments: steps in the test
  class, a German feature with glue in another package, failure reporting and
  Arquillian events.

## Migrating from 1.x

| 1.x | 2.x |
|---|---|
| `@RunWith(CukeSpace.class)` / `@RunWith(ArquillianCucumber.class)` | `extends CukeSpaceTest`, or `@ExtendWith(ArquillianExtension.class)` + `CukeSpace.runFeatures(this)` |
| TestNG `extends CukeSpace` | not supported anymore |
| `@cucumber.api.CucumberOptions`, `@Features`, `@Glues`, `@Tags` | `@com.github.cukespace.CucumberOptions(features, glue, extraGlue, tags)` |
| `cucumber.api.java.en.*`, `cucumber.api.java.*` | `io.cucumber.java.en.*`, `io.cucumber.java.*` |
| regular expressions in step annotations | still supported, Cucumber expressions recommended |
| `arquillian.xml`: `options`, `featureHome`, `tempDir`, `objectFactory` | Cucumber properties (`cucumber.filter.tags`, `cucumber.features`, `cucumber.object-factory`, ...) |
| `arquillian.xml`: `report`, `reportDirectory`, `generateDocs` (cucumber-reporting, cukedoctor) | Cucumber plugins, e.g. `cucumber.plugin=html:target/cucumber.html` |
| Arquillian Persistence integration (`persistenceEventsActivated`) | removed (the extension is not maintained) |
| `@Lambda` / cucumber-java8 | removed |
| `cucumber.runtime.arquillian.api.event.*` | `com.github.cukespace.api.event.*` |
