package cuke4duke.internal.language;

import cuke4duke.Scenario;

import java.util.List;

public interface Hook {
    public List<String> getTagExpressions();

    void invoke(String location, Scenario scenario) throws Throwable;
}
