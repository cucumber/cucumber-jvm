To *build* all android example modules with maven:

```
mvn package -pl examples/android -am -amd -P android,android-examples
```

To *clean* all android example modules with maven:

```
mvn clean -pl examples/android -amd -P android-examples
```

The example projects depend on the current (unreleased) Cucumber-JVM modules.
If any of the examples fail to build, just build the android module and its dependencies once first:

```
mvn clean install -pl android -am
```

To create a virtual device and start an [Android emulator](https://developer.android.com/tools/devices/index.html):

```
$ANDROID_HOME/tools/android avd
```
