## Developers

### Prerequisites

Taken from the [maven-android-plugin](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted) documentation.

1. JDK 1.6+ installed as required for Android development
2. [Android SDK](http://developer.android.com/sdk/index.html) (r21 or later, latest is best supported) installed, preferably with all platforms.

Integration-tests are in `examples/android/android-test/cucumber-test/`.

### Building

```sh
mvn package -pl android -am
```

### Using Cucumber-Android
1. Include the following jars into your Android project either through Maven or directly copy into libs folder: cucumber-android.jar, cucumber-core.jar, cucumber-html.jar, cucumber-java.jar, cucumber-junit.jar, cucumber-jvm-deps.jar, gherkin.jar. Also cucumber-picocontainer.jar and picocontainer.jar if you want to use picocontainer. You can download the jar files from the [public maven repo](http://repo1.maven.org/maven2/info/cukes/)

2. Create a class that extends TestCase or any of its subclasses, and add @CucumberOptions annotation to that class. This class doesn't need to have anything in it, but you can also put some codes in it if you want. The purpose of doing this is to provide cucumber options. A simple example can be found at cucumber-jvm / examples / android / android-test / src / cucumber / example / android / test / CucumberActivitySteps.java. Or a more complicated example here:
```java
@CucumberOptions(glue = "com.mytest.steps", format = {"junit:/data/data/com.mytest/JUnitReport.xml", "json:/data/data/com.mytest/JSONReport.json"}, tags = { "~@wip" }, features = "features")
public class MyTests extends TestCase
{
}
```
glue is the path to step definitions, format is the path for report outputs, tags is the tags you want cucumber-android to run or not run, features is the path to the feature files.  
You can also use command line to provide these options to cucumber-android. Here is the detailed documentation on how to use command line to provide these options: [Command Line Options for Cucumber Android](https://github.com/cucumber/cucumber-jvm/pull/597)

3. Write your .feature files under your test project's assets/<features-folder> folder. If you specify features = "features" like the example above then it's assets/features.

4. Write your step definitions under the package name specified in glue. For example, if you specified glue = "com.mytest.steps", then create a new package under your src folder named "com.mytest.steps" and put your step definitions under it. Note that all subpackages will also be included, so you can also put in "com.mytest.steps.mycomponent".

5. Add the followings in your test project's AndroidManifest.xml
```xml
<instrumentation
    android:name="cucumber.api.android.CucumberInstrumentation"
    android:targetPackage="<Your tested application package name>" />

<application
    android:icon="@drawable/ic_launcher"
    android:label="@string/app_name" >
    <uses-library android:name="android.test.runner" />
</application>
```

6. If you are running from Eclipse, create a new Android JUnit run configuration, select "Run all tests in the selected project or package", and select your test project. It's recommended to select the package where the class with @CucumberOptions is in, because the cucumber-android will scan all files you give it for that class. If you specify the whole project, it will take long for cucumber-android to find that class. Sellect cucumber.api.android.CucumberInstrumentation for the Instrumentation runner.

### Debugging
Please read [the Android documentation on debugging](https://developer.android.com/tools/debugging/index.html).
