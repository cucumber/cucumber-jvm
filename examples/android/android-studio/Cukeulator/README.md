## Cukeulator Example Test
This is the example test-project for the Cukeulator app for Android Studio 1.0.2.

### Setup
Features must be placed in `assets/features/`. Subdirectories are allowed.

The rest of the dependencies are added automatically in `app/build.gradle`.

The cucumber-android dependency is added as:

```
androidTestCompile 'info.cukes:cucumber-android:1.2.0@jar'
```

Note, the `@jar` suffix is required in order to use the embedded jar file.

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
instrumentation:cukeulator.android.example.cucumber.cukeulator.test/cucumber.api.android.CucumberInstrumentation (target=cukeulator.android.example.cucumber.cukeulator)
```

To run the test:

```
cd cucumber-jvm/examples/android/android-studio/Cukeulator;
./gradlew connectedCheck
```

As an alternative option, the test can be run with adb:

```
adb shell am instrument -w cukeulator.android.example.cucumber.cukeulator.test/cucumber.api.android.CucumberInstrumentation
```

### Using an Android Studio IDE
1. Import the example to Android Studio: `File > Import Project`.
2. Make sure you have the cucumber-android jar dependencies in `app/libs/`.
3. Create a test run confiruation:
    1.  Run > Edit Configurations
    2. Click `+` button and select Android Tests
    3. Specify test name: `CalculatorTest`
    4. Select module: `app`
    5. Enter a Specific instrumentation runner: `cucumber.api.android.CucumberInstrumentation`
    6. Click Ok

### Output
Filter for the logcat tag `cucumber-android` in [DDMS](https://developer.android.com/tools/debugging/ddms.html).
