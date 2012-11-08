package cucumber.runtime.junit;

import cucumber.api.junit.Cucumber;
import cucumber.runtime.RuntimeOptions;
import org.junit.Assert;
import org.junit.Test;

public class RuntimeOptionsFactory_AppendStarterClassToFeaturePathsTest {

    @Cucumber.Options()
    static class DefaultOptions {

    }

    @Test
    public void do_not_alter_feature_path_by_default() throws Exception {
        RuntimeOptionsFactory factory = new RuntimeOptionsFactory(DefaultOptions.class);
        RuntimeOptions runtimeOptions = factory.create();
        Assert.assertEquals("classpath:cucumber/runtime/junit", runtimeOptions.featurePaths.get(0));
    }

    @Cucumber.Options(appendStarterClassToFeaturePaths = true)
    static class Append {

    }

    @Test
    public void append_starter_class_name_as_feature_file() throws Exception {
        RuntimeOptionsFactory factory = new RuntimeOptionsFactory(Append.class);
        RuntimeOptions runtimeOptions = factory.create();
        Assert.assertEquals("classpath:cucumber/runtime/junit/Append.feature", runtimeOptions.featurePaths.get(0));
    }

    @Cucumber.Options(appendStarterClassToFeaturePaths = true, features = {"one/two/", "classpath:three/four/"})
    static class AppendToAllFeaturePathsPointingToADirectory {

    }

    @Test
    public void append_starter_class_name_as_feature_file_to_each_feature() throws Exception {
        RuntimeOptionsFactory factory = new RuntimeOptionsFactory(AppendToAllFeaturePathsPointingToADirectory.class);
        RuntimeOptions runtimeOptions = factory.create();
        Assert.assertEquals("one/two/AppendToAllFeaturePathsPointingToADirectory.feature", runtimeOptions.featurePaths.get(0));
        Assert.assertEquals("classpath:three/four/AppendToAllFeaturePathsPointingToADirectory.feature", runtimeOptions.featurePaths.get(1));
    }

    @Cucumber.Options(appendStarterClassToFeaturePaths = true, features = {"five/six.feature", "classpath:seven/eight.txt"})
    static class DoNotAppendToFeaturePathsPointingToAFile {

    }

    @Test
    public void do_not_append_starter_class_name_as_feature_file() throws Exception {
        RuntimeOptionsFactory factory = new RuntimeOptionsFactory(DoNotAppendToFeaturePathsPointingToAFile.class);
        RuntimeOptions runtimeOptions = factory.create();
        Assert.assertEquals("five/six.feature", runtimeOptions.featurePaths.get(0));
        Assert.assertEquals("classpath:seven/eight.txt", runtimeOptions.featurePaths.get(1));
    }
}