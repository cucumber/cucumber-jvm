## Cukeulator Example Test
This is the example test-project for the Cukeulator app.

### Setup
Features must be placed in `assets/features/`. Subdirectories are allowed.

Read `libs/README.md` for details on dependencies.

### Using Ant
1. Please read ["Building and Running from the Command Line"](https://developer.android.com/tools/building/building-cmdline.html).
2. Run `ant test`.

### Using an IDE
1. Please read ["Building and Running from Eclipse with ADT"](https://developer.android.com/tools/building/building-eclipse.html).
2. Create an Android test-project from these sources with `cucumber-example/` as the tested project.
3. Create a run configuration with `cucumber.android.api.CucumberInstrumentation` as the instrumentation.
4. Make sure you have the required jar dependencies in `libs/`.

### Using Maven
To build:

```
mvn package -pl examples/android/cukeulator-test -am -P android,android-examples
```

To intall and run:

```
mvn install -pl examples/android/cukeulator-test -am -P android-examples
```

To re-run already installed package:

```
mvn android:instrument -pl examples/android/cukeulator-test -P android-examples
```

View [all available goals](http://maven-android-plugin-m2site.googlecode.com/svn/plugin-info.html):

```
mvn android:help -pl examples/android/cukeulator-test -P android-examples
```

### Output
Filter for the logcat tag `cucumber-android` in [DDMS](https://developer.android.com/tools/debugging/ddms.html).
