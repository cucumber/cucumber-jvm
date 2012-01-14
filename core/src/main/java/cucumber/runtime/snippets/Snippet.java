package cucumber.runtime.snippets;

import java.util.List;

public interface Snippet {
    String template();

    String arguments(List<Class<?>> argumentTypes);

    /**
     * Langauges that don't support named capture groups should return null.
     * @return the start of a named group
     */
    String namedGroupStart();

    String namedGroupEnd();

    String escapePattern(String pattern);
}
