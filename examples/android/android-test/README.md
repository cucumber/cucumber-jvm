## Developers
This maven module contains an Android project with integration tests for cucumber-android.

### Prerequisites *(taken from the [maven-android-plugin](https://code.google.com/p/maven-android-plugin) documentation)*
1. JDK 1.6+ installed as required for Android development
2. [Android SDK](http://developer.android.com/sdk/index.html) (r17 or later, latest is best supported) installed, preferably with all platforms.
3. [Maven 3.0.3+](http://maven.apache.org/download.html) installed
4. Set environment variable `ANDROID_HOME` to the path of your installed Android SDK and add `$ANDROID_HOME/tools` as well as `$ANDROID_HOME/platform-tools` to your `$PATH`.

On Windows: use `%ANDROID_HOME%\tools` and `%ANDROID_HOME%\platform-tools` instead.

On OS X: Note that for the path to work on the commandline and in IDE's started by launchd [you have to set it](http://stackoverflow.com/questions/135688/setting-environment-variables-in-os-x/588442) in `/etc/launchd.conf` and **NOT** in .bashrc or something else.

### Building
**Using Maven:**

`mvn package -pl examples/android/android-test -am -P android,android-examples`

### Debugging
Please read [the Android documentation on debugging](https://developer.android.com/tools/debugging/index.html).

**Using Maven:**

`mvn install -pl examples/android/android-test -am -P android,android-examples`

**Using [adb](https://developer.android.com/tools/testing/testing_otheride.html#AMSyntax) on the commandline:**

`adb install -r examples/android/android-test/target/cucumber-android-test-*.apk`

`adb shell am instrument -w -r cucumber.android.test/cucumber.api.android.CucumberInstrumentation`

**Using Ant:**

Please read ["Testing from Other IDEs"](https://developer.android.com/tools/testing/testing_otheride.html).

`TODO: provide standard ant buildfile and custom script for dependency downloading`

**Using an IDE:**

[Set up your IDE](https://developer.android.com/sdk/installing/index.html) and import this directory as a
new Android test-project. You will also need to create a new run-configuration with `CucumberInstrumentation`.
Please also refer to ["Testing from Eclipse with ADT"](https://developer.android.com/tools/testing/testing_eclipse.html).