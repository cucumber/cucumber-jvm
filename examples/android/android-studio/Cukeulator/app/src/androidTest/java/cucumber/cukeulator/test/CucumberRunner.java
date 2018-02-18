package cucumber.cukeulator.test;

import android.os.Bundle;
import android.os.Environment;
import android.support.test.runner.AndroidJUnitRunner;

import java.io.File;

import cucumber.api.CucumberOptions;
import cucumber.api.android.CucumberInstrumentationCore;

/**
 * A modern replacement for {@link cucumber.api.android.CucumberInstrumentation}.
 * Supports Cucumber steps without base classes plus activity test rules.
 * <p/>
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown.
 */
@CucumberOptions
public class CucumberRunner extends AndroidJUnitRunner {

    private final CucumberInstrumentationCore instrumentationCore;

    public CucumberRunner() {
        instrumentationCore = new CucumberInstrumentationCore(this);
    }

    @Override
    public void onCreate(final Bundle bundle) {
        bundle.putString("features", "features");
        bundle.putString("plugin", getPluginConfigurationString());
        instrumentationCore.create(bundle);
        super.onCreate(bundle);
    }

    @Override
    public void onStart() {
        waitForIdleSync();
        instrumentationCore.start();
    }

    /**
     * Since we want to checkout the external storage directory programmatically, we create the plugin configuration
     * here, instead of the {@link CucumberOptions} annotation.
     * @return the plugin string for the configuration, which contains XML, HTML and JSON paths
     */
    private String getPluginConfigurationString() {
        final String cucumber = "cucumber";
        final String separator = "--";
        return
            "junit:" + getAbsoluteFilesPath() + "/" + cucumber + ".xml" + separator +
            "html:" + getAbsoluteFilesPath() + "/" + cucumber + ".html" + separator +
            "json:" + getAbsoluteFilesPath() + "/" + cucumber + ".json";
    }

    /**
     * The path which is used for the report files.
     * @return the absolute path for the report files
     */
    private String getAbsoluteFilesPath() {
        // Since stupidly, connected-check tasks uninstall the applications,
        // we have to find a directory outside the application directory.
        File directory = Environment.getExternalStorageDirectory();
        return new File(directory, "/cucumber").getAbsolutePath();
    }

}