# tycho-surefire example

Eclipse Instructions:

After importing the project into the workspace, perform these manual steps:

1) In the `tycho-surefire-example-target` directory, open the `tycho-surefire.target` file.
2) Allow the target to load.  If it doesn't load automatically, click the `Reload` button.
3) In the top right corner, click the `Set as Target Platform` link.

To compile and run just this example, you must execute the `Run As -> Maven Install` from the `tycho-surefire` parent module level.

To execute just the tests within the example, execute the `Run As -> JUnit Test` from the `tycho-surefire-example-tests` module level.  The expected results of this, however, are for the Scenarios of the feature file to be displayed in the JUnit view, but for no tests to actually run because it was not executed inside an OSGi container.
