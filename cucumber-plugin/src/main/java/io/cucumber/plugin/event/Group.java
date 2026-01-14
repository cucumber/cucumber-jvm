package io.cucumber.plugin.event;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * A capture group in a Regular or Cucumber Expression.
 */
public interface Group {

    Collection<Group> getChildren();

    @Nullable
    String getValue();

    int getStart();

    int getEnd();

}
