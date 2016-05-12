# Step dependencies

The picocontainer will create singleton instances of any Step class dependencies which are constructor parameters and inject them into the Step class instances when constructing them.

# Step scope and lifecycle

All step classes and their dependencies will be recreated fresh for each scenario, even if the scenario in question does not use any steps from that particular class.

If any step classes or dependencies use expensive resources (such as database connections), you should create them lazily on-demand, rather than eagerly, to improve performance.

The pico container created by `cucumber-jvm` uses a `NullLifecycleStrategy` (see http://picocontainer.com/lifecycle.html ) so the Startable or Disposeable interfaces will be ignored. You should use cucumber.api.java.After annotations to do any necessary cleanup.
