package cucumber.api;

/**
 * Represents an argument in for a step definition.
 * <p>
 * The step definition {@code I have {long} cukes in my belly}
 * when matched with {@code I have 7 cukes in my belly} will produce
 * one argument with value {@code "4"}, starting at {@code 7} and
 * ending at {@code 8}.
 */
public interface Argument {
    String getValue();

    int getStart();

    int getEnd();
}
