This module uses the [maven-android-plugin](https://code.google.com/p/maven-android-plugin).

To execute the tests on the [Android emulator](http://developer.android.com/tools/devices/emulator.html) run:

`mvn install -pl examples/android-test -am -P android,android-examples`

Or to manually install the apk and run the tests on the command line [with adb](https://developer.android.com/tools/testing/testing_otheride.html#AMSyntax):

`mvn package -pl examples/android-test -am -P android,android-examples`
`adb install examples/android-test/target/cucumber-android-test-*.apk`
`adb shell am instrument -w -r cucumber.android.test/cucumber.api.android.CucumberInstrumentation`
