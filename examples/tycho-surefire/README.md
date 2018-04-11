# tycho-surefire example

Eclipse Instructions:

After importing the project into the workspace, perform these manual steps in Eclipse:

1) In the `tycho-surefire-example-target` project, double-click the `tycho-surefire.target` file to open it.
2) Allow the target to load.  If it doesn't load automatically, click the `Reload` button.
3) In the top right corner, click the `Set as Target Platform` link.

To compile and run just this example, you must execute the `Run As -> Maven Install` from the `tycho-surefire` parent module level.

If you execute `Run As -> JUnit Test` from the `tycho-surefire-example-test` module, the expected results are for the Scenarios of the feature file to be displayed in the JUnit view, but for no tests to actually run because it was not executed inside an OSGi container.
