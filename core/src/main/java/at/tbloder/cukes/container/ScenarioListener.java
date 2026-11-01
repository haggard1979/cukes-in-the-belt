package at.tbloder.cukes.container;

import at.tbloder.cukes.ScenarioFailedError;
import at.tbloder.cukes.api.event.AfterScenario;
import at.tbloder.cukes.api.event.AfterStep;
import at.tbloder.cukes.api.event.BeforeScenario;
import at.tbloder.cukes.api.event.BeforeStep;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects the failed scenarios and forwards the Cucumber events as Arquillian events.
 */
final class ScenarioListener implements ConcurrentEventListener {
    private final List<ScenarioFailedError> failures = Collections.synchronizedList(new ArrayList<>());
    private final Map<Object, Set<String>> undefinedSteps = new ConcurrentHashMap<>();
    private final Set<String> snippets = Collections.synchronizedSet(new LinkedHashSet<>());
    private volatile int scenarios;

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::testCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::testStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::testStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::testCaseFinished);
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, e -> snippets.addAll(e.getSnippets()));
    }

    List<ScenarioFailedError> getFailures() {
        return failures;
    }

    int getScenarioCount() {
        return scenarios;
    }

    Set<String> getSnippets() {
        return snippets;
    }

    private void testCaseStarted(final TestCaseStarted event) {
        final TestCase testCase = event.getTestCase();
        CukesLifecycle.fire(new BeforeScenario(testCase.getName(), testCase.getUri(), testCase.getLocation().getLine()));
    }

    private void testStepStarted(final TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            final TestCase testCase = event.getTestCase();
            CukesLifecycle.fire(new BeforeStep(testCase.getName(), testCase.getUri(), testCase.getLocation().getLine(),
                    step.getStep().getKeyword(), step.getStep().getText(), step.getStep().getLine()));
        }
    }

    private void testStepFinished(final TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            final TestCase testCase = event.getTestCase();
            final Result result = event.getResult();
            if (result.getStatus() == Status.UNDEFINED) {
                undefinedSteps.computeIfAbsent(testCase.getId(), k -> Collections.synchronizedSet(new LinkedHashSet<>()))
                        .add(step.getStep().getKeyword() + step.getStep().getText() + " (line " + step.getStep().getLine() + ")");
            }
            CukesLifecycle.fire(new AfterStep(testCase.getName(), testCase.getUri(), testCase.getLocation().getLine(),
                    step.getStep().getKeyword(), step.getStep().getText(), step.getStep().getLine(),
                    result.getStatus().name(), result.getError()));
        }
    }

    private void testCaseFinished(final TestCaseFinished event) {
        scenarios++;
        final TestCase testCase = event.getTestCase();
        final Result result = event.getResult();
        final Set<String> undefined = undefinedSteps.remove(testCase.getId());
        if (!result.getStatus().isOk()) {
            failures.add(new ScenarioFailedError(message(testCase, result, undefined), result.getError()));
        }
        CukesLifecycle.fire(new AfterScenario(testCase.getName(), testCase.getUri(), testCase.getLocation().getLine(),
                result.getStatus().name(), result.getError()));
    }

    private static String message(final TestCase testCase, final Result result, final Set<String> undefined) {
        final StringBuilder message = new StringBuilder()
                .append(testCase.getKeyword()).append(": ").append(testCase.getName())
                .append(" (").append(testCase.getUri()).append(':').append(testCase.getLocation().getLine()).append(") ")
                .append(result.getStatus());
        if (undefined != null && !undefined.isEmpty()) {
            message.append(" - undefined steps: ").append(String.join(", ", undefined));
        }
        final Throwable error = result.getError();
        if (error != null) {
            message.append(System.lineSeparator()).append("  ").append(error.getClass().getName());
            if (error.getMessage() != null) {
                message.append(": ").append(error.getMessage());
            }
        }
        return message.toString();
    }
}
