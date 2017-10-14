package cucumber.java;

import java.util.List;

public class StepMatch {
    private String id;
    private List<StepMatchArg> args;
    private String source;
    private String regexp;

    public StepMatch(String id, List<StepMatchArg> args, String source, String regexp) {
        this.id = id;
        this.args = args;
        this.source = source;
        this.regexp = regexp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<StepMatchArg> getArgs() {
        return args;
    }

    public void setArgs(List<StepMatchArg> args) {
        this.args = args;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }
}
