package cucumber.testng;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;

import cucumber.formatter.ColorAware;
import cucumber.io.ClasspathResourceLoader;
import cucumber.io.FileResourceLoader;
import cucumber.io.MultiLoader;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.ObjectFactoryHolder;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;

public class TestFactory {

    private Reporter reporter;
    private Formatter formatter;
    
    @Factory
    public Object[] getCucumberTests() throws IOException {
      List<Object> tests = new ArrayList<Object>();
      RuntimeOptions options = getOptions(this.getClass());
      formatter = options.formatter(this.getClass().getClassLoader());
      reporter = options.reporter(this.getClass().getClassLoader());
      Runtime runtime = new Runtime(new FileResourceLoader(), Thread.currentThread().getContextClassLoader(), options);
      runtime.writeStepdefsJson();
      List<CucumberFeature> features = options.cucumberFeatures(new MultiLoader( Thread.currentThread().getContextClassLoader()));
      System.out.println("features: " +features.size());
      List<CucumberScenario> scenarios = new ArrayList<CucumberScenario>();
      for (CucumberFeature feature : features){
        for (CucumberTagStatement tagStatement : feature.getFeatureElements()){
          if (CucumberScenario.class.isAssignableFrom(tagStatement.getClass())){
            scenarios.add((CucumberScenario) tagStatement);
          } else if (CucumberScenarioOutline.class.isAssignableFrom(tagStatement.getClass())){
            CucumberScenarioOutline outline = (CucumberScenarioOutline) tagStatement;
            for (CucumberExamples example : outline.getCucumberExamplesList()){
              scenarios.addAll(example.createExampleScenarios());
            }
          } else {
            throw new RuntimeException("should never be reachable");
          }
        }
      }
      for (CucumberScenario cucumberScenario : scenarios){
          TestRunner callable = new TestRunner(cucumberScenario, reporter, formatter, runtime);
          tests.add(callable);
      }
      System.out.println("Scenarios: "+tests.size());
      return tests.toArray(new Object[tests.size()]);
    }
    
    
    /**
     * Instance ObjectFactory up front. In our case, this will be Spring's Factory...we may need to find it later and
     * unless we do this explicitly ourselves and place it in ObjectFactoryHolder, cucumber will keep ObjectFactory
     * hidden/private
     */
    private void exposeObjectFactory(){
      ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader());
      ObjectFactory of = classpathResourceLoader.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
      ObjectFactoryHolder.setFactory(of);
    }
        
    public RuntimeOptions getOptions(Class<?> testClass){
      try {
        Class runtimeOptionFactoryClass = Class.forName("cucumber.junit.RuntimeOptionsFactory");
        Constructor<?> constructor = runtimeOptionFactoryClass.getConstructor(this.getClass().getClass());
        constructor.setAccessible(true);
        Object o = constructor.newInstance(testClass);
        Method createMethod = runtimeOptionFactoryClass.getMethod("create", new Class[]{});
        createMethod.setAccessible(true);
        Object output = createMethod.invoke(o, new Object[]{});
        return (RuntimeOptions) output;
      } catch (Exception e){
        throw new RuntimeException("Looks like the RuntimeOptionsFactory has changed. Fuck", e);
      }
    }
  
}
