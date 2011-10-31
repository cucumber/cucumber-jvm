package cucumber.runtime.model;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.Set;

public class ExampleStep extends Step {
    // TODO: Use this to colour columns in associated Example row with our associated status.
    private final Set<Integer> matchedColumns;

    public ExampleStep(List<Comment> comments, String keyword, String name, int line, Set<Integer> matchedColumns) {
        super(comments, keyword, name, line);
        this.matchedColumns = matchedColumns;
    }
}
