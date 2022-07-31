package io.cucumber.plugin.event;

import java.util.Collection;

/**
 * A capture group in a Regular or Cucumber Expression.
 */
public interface Group {

    Collection<Group> getChildren();

    String getValue();

    int getStart();

    int getEnd();

}
