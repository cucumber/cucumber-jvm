package cucumber.cli;

import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.resources.Consumer;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.util.ArrayList;
import java.util.List;

public class Runner {
    private final Runtime runtime;
    private final List<String> filesOrDirs;
    private final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
    private final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
    private final List<Object> filters;

    public Runner(Runtime runtime, List<String> filesOrDirs, List<Object> filters) {
        this.runtime = runtime;
        this.filesOrDirs = filesOrDirs;
        this.filters = filters;
    }

    public void run(Formatter formatter, Reporter reporter) {
        // Parse all gherkin files and build CucumberFeatures
        for (String fileOrDir : filesOrDirs) {
            Resources.scan(fileOrDir, ".feature", new Consumer() {
                @Override
                public void consume(Resource resource) {
                    builder.parse(resource, filters);
                }
            });
        }
        
        //traverse(filesOrDirs.toArray(new String[filesOrDirs.size()]), filters);

        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            cucumberFeature.run(runtime, formatter, reporter);
        }
    }

//    private void traverse(String[] filesOrDirs, final List<Object> filters) {
//        for (String fileOrDir : filesOrDirs) {
//            final PathWithFilters pathWithFilters = new PathWithFilters(fileOrDir, filters);
//
//            File file = new File(pathWithFilters.path);
//            if (file.exists()) {
//                // Read it directly from the file system
//                traverse(file, filters);
//            } else {
//                // Read it from the classpath
//                Consumer consumer = new Consumer() {
//                    @Override
//                    public void consume(Resource resource) {
//                        builder.parse(resource, pathWithFilters.filters);
//                    }
//                };
//                if (fileOrDir.endsWith(".feature")) {
//                    Classpath.scan(fileOrDir, consumer);
//                } else {
//                    Classpath.scan(fileOrDir, ".feature", consumer);
//                }
//            }
//        }
//    }
//
//    private void traverse(File fileOrDir, List<Object> filters) {
//        if (fileOrDir.isDirectory()) {
//            File[] files = fileOrDir.listFiles();
//            for (File file : files) {
//                traverse(file, filters);
//            }
//        } else if (fileOrDir.isFile() && fileOrDir.getName().endsWith(".feature")) {
//            addFeature(fileOrDir, filters);
//        }
//    }
//
//    private void addFeature(File path, List<Object> filters) {
//        File rootDir = path.getParentFile(); // TODO: use current dir, and fix FileResource to deal with incompatible paths
//        Resource resource = new FileResource(rootDir, path, filters);
//        builder.parse(resource, filters);
//    }

}
