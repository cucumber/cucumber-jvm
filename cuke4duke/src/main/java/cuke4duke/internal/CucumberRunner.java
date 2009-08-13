package cuke4duke.internal;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.jruby.javasupport.bsf.JRubyEngine;

import java.net.URL;

public class CucumberRunner {
//    private final AnnotationStepMother stepMother;
    private JRubyEngine rubyEngine;
    private BSFManager bsfManager;

//    public CucumberRunner(AnnotationStepMother stepMother) throws BSFException {
//        this.stepMother = stepMother;
//        initializeCucumber();
//    }

    private void initializeCucumber() throws BSFException {
        if (System.getProperty("jruby.home") == null) {
            if (System.getenv("JRUBY_HOME") != null) {
                System.setProperty("jruby.home", System.getenv("JRUBY_HOME"));
            } else {
                throw new RuntimeException("Missing system jruby.home property or JRUBY_HOME enironment variable");
            }
        }

        BSFManager.registerScriptingEngine("ruby", JRubyEngine.class.getName(), new String[]{"rb"});
        bsfManager = new BSFManager();
//        bsfManager.declareBean("cuke4duke_step_mother", stepMother, stepMother.getClass());
        rubyEngine = (JRubyEngine) bsfManager.loadScriptingEngine("ruby");
        String script =
            "require 'rubygems'\n" +
            "require 'cuke4duke/java'\n" +
            "self.step_mother = $cuke4duke_step_mother\n";
        rubyEngine.exec("CucumberRunner", 0, 0, script);
    }

    public void run(String featurePath, String scenarioName, Visitor visitor) throws BSFException {
        bsfManager.declareBean("cuke4duke_visitor_delegate", visitor, visitor.getClass());
        String fullFeaturePath = fullFeaturePath(featurePath);

        String mainScript =
          "Cucumber::Cli::Main.execute(['--format', 'Cuke4duke::Formatter', '--name', '" + scenarioName + "', '" + fullFeaturePath + "'])\n";
        try {
            rubyEngine.exec("CucumberRunner.run", 0, 0, mainScript);
        } catch(BSFException e) {
            throw new RuntimeException("Script failed:\n" + mainScript, e);
        }
    }

    private String fullFeaturePath(String featurePath) {
        URL featureURL = getClass().getResource(featurePath);
        if(featureURL == null) {
            throw new RuntimeException("Couldn't find " + featurePath + " on the classpath.");
        } else {
            return featureURL.getPath();
        }
    }
}
