## Cukeulator Example App
This is a simple Android example application for illustration purposes.

### Build with ant
See ["Building and Running from the Command Line"](https://developer.android.com/tools/building/building-cmdline.html).

### Build with Eclipse
See ["Building and Running from Eclipse with ADT"](https://developer.android.com/tools/building/building-eclipse.html).

### Build with Maven
To build:

```
mvn package -pl examples/android/cukeulator -am -P android-examples
```

To install:

```
mvn android:deploy -pl examples/android/cukeulator -P android-examples
```

To run:

```
mvn android:run -pl examples/android/cukeulator -P android-examples
```

View [all available goals](http://maven-android-plugin-m2site.googlecode.com/svn/plugin-info.html):

```
mvn android:help -pl examples/android/cukeulator -P android-examples
```
