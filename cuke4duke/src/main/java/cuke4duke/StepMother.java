package cuke4duke;

public interface StepMother {
    void invoke(String step);

    void invoke(String step, Table table);

    void invoke(String step, String multilineString);

    /**
     * Suspends execution and asks for input. This is known to work witn Ant,
     * but not with Maven, which seems to mess up STDIN.
     *
     * @param question    a question to print to the console
     * @param timeoutSecs number of seconds to wait before a timeout exception occurs.
     * @return the line of text that the user wrote.
     */
    String ask(String question, int timeoutSecs);

    /**
     * Output a message alongside the formatted output.
     * This is an alternative to using System.out.println - it will display
     * nicer, and in all outputs (in case you use several formatters)
     *
     * @param message what to print.
     */
    void announce(String message);

    /**
     * Embed a file in the formatter outputs. This may or may
     * not be ignored, depending on what kind of formatter(s) are active.
     */
    void embed(String file, String mimeType);
}
