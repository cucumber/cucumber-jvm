## Cukeulator Example Test
This is the example test-project for the Cukeulator app for Android Studio 1.0.2.

### Setup
Features must be placed in `assets/features/`. Subdirectories are allowed.

The rest of the dependencies are added automatically in `app/build.gradle`.

The cucumber-android dependency is added as (see `app/build.gradle`):

```
androidTestCompile 'info.cukes:cucumber-android:<version>'
```

### Using gradle

To build the test apk:

```
cd cucumber-jvm/examples/android/android-studio/Cukeulator

./gradlew --parallel :app:assembleDebugTest
```

The build generates an apk in app/build/outputs/apk/app-debug.apk.

To install the apk on a device:

```
adb install -r app/build/outputs/apk/app-debug.apk
```

To verify that the test is installed, run:

```
adb shell pm list instrumentation
```

The command output should display;

```
instrumentation:cucumber.cukeulator.test/cucumber.api.android.CucumberInstrumentation (target=cucumber.cukeulator)
```

To run the test:

```
cd cucumber-jvm/examples/android/android-studio/Cukeulator;
./gradlew connectedCheck
```

As an alternative option, the test can be run with adb:

```
adb shell am instrument -w cucumber.cukeulator.test/cucumber.cukeulator.test.Instrumentation
```

### Using an Android Studio IDE
1. Import the example to Android Studio: `File > Import Project`.
2. Create a test run configuration:
    1.  Run > Edit Configurations
    2. Click `+` button and select Android Tests
    3. Specify test name: `CalculatorTest`
    4. Select module: `app`
    5. Enter a Specific instrumentation runner: `cucumber.cukeulator.test.Instrumentation`
    6. Click Ok

### Output
Filter for the logcat tag `cucumber-android` in [DDMS](https://developer.android.com/tools/debugging/ddms.html).
