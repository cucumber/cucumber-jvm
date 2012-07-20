package cucumber.runtime.snippets;

import java.util.List;

public interface Snippet {
    /**
     * @return a {@link java.text.MessageFormat} template used to generate a snippet. The template can access the following variables:
     *         <p/>
     *         <ul>
     *         <li>{0} : Step Keyword</li>
     *         <li>{1} : Value of {@link #escapePattern(String)}</li>
     *         <li>{2} : Function name</li>
     *         <li>{3} : Value of {@link #arguments(java.util.List)}</li>
     *         <li>{4} : Regexp hint comment</li>
     *         <li>{5} : value of {@link #tableHint()} if the step has a table</li>
     *         </ul>
     */
    String template();

    /**
     * @return a hint about alternative ways to declare a table argument
     */
    String tableHint();

    /**
     * @param argumentTypes the types the snippet's argument should accept
     * @return a string representation of the arguments
     */
    String arguments(List<Class<?>> argumentTypes);

    /**
     * Langauges that don't support named capture groups should return null.
     *
     * @return the start of a named capture group
     */
    String namedGroupStart();

    /**
     * Langauges that don't support named capture groups should return null.
     *
     * @return the end of a named capture group
     */
    String namedGroupEnd();

    /**
     * @param pattern the computed pattern that will match an undefined step
     * @return an escaped representation of the pattern, if escaping is necessary.
     */
    String escapePattern(String pattern);
}
