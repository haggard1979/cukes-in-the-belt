package at.tbloder.cukes;

/**
 * Reports a scenario that did not pass. Only text and stack trace of the
 * original failure are kept, so the error can always be serialized from the
 * container back to the client.
 */
public final class ScenarioFailedError extends AssertionError {
    private static final long serialVersionUID = 1L;

    public ScenarioFailedError(final String message, final Throwable original) {
        super(message);
        if (original != null) {
            setStackTrace(original.getStackTrace());
        }
    }
}
