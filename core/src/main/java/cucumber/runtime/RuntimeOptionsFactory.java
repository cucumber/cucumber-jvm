package cucumber.runtime;

import static java.util.Arrays.asList;
import cucumber.api.CucumberConfiguration;
import cucumber.api.CucumberConfiguration.Type;
import cucumber.api.CucumberOptions;
import cucumber.runtime.configuration.YamlConfigurationParser;
import cucumber.runtime.io.MultiLoader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuntimeOptionsFactory {
  private static final String FILE_PREFIX = "file:";
  private static final String CLASSPATH_PREFIX = "classpath:";
  private final Class<?> clazz;
  private boolean featuresSpecified = false;
  private boolean overridingGlueSpecified = false;

  public RuntimeOptionsFactory(Class<?> clazz) {
    this.clazz = clazz;
  }

  public RuntimeOptions create() {
    CucumberConfiguration configuration = clazz.getAnnotation(CucumberConfiguration.class);

    // TODO: evaluate annotations on super classes
    if (configuration == null || configuration.type().equals(Type.ANNOTATION)) {
      return new RuntimeOptions(buildArgsFromOptions());
    }
    Reader configurationReader = null;
    try {
      configurationReader = getConfigurationReader(configuration.path());
      switch (configuration.type()) {
        case YAML:
          ConfigurationParser configurationParser =
              new YamlConfigurationParser(configurationReader);
          return new RuntimeOptions(configurationParser.getMap());

        case JSON:
          throw new UnsupportedOperationException("JSON configuration is not yet supported");

        case PROPERTIES:
          throw new UnsupportedOperationException("Properties configuration is not yet supported");

        default:
          return new RuntimeOptions(buildArgsFromOptions());
      }
    } finally {
      if (configurationReader != null) {
        try {
          configurationReader.close();
        } catch (IOException e) {
          // too bad
        }
      }
    }
  }

  private Reader getConfigurationReader(String path) {
    if (path.startsWith(FILE_PREFIX)) {
      try {
        return new FileReader(path.replaceFirst("^" + FILE_PREFIX, ""));
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException(
            String.format("CucumberConfiguration file %s missing!", path));
      }
    } else {
      InputStream inputStream =
          getClass().getResourceAsStream(path.replaceFirst("^" + CLASSPATH_PREFIX, ""));

      if (inputStream == null) {
        throw new IllegalArgumentException(
            String.format("CucumberConfiguration file %s not found on classpath!", path));
      }
      return new InputStreamReader(inputStream);
    }
  }

  private List<String> buildArgsFromOptions() {
    List<String> args = new ArrayList<String>();

    for (Class classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions =
        classWithOptions.getSuperclass()) {
      CucumberOptions options = getOptions(classWithOptions);
      if (options != null) {
        addDryRun(options, args);
        addMonochrome(options, args);
        addTags(options, args);
        addPlugins(options, args);
        addStrict(options, args);
        addName(options, args);
        addSnippets(options, args);
        addGlue(options, args);
        addFeatures(options, args);
        addJunitOptions(options, args);
      }
    }
    addDefaultFeaturePathIfNoFeaturePathIsSpecified(args, clazz);
    addDefaultGlueIfNoOverridingGlueIsSpecified(args, clazz);
    return args;
  }

  private void contributeOptions(CucumberOptions options, List<String> args) {
    addName(options, args);
    addSnippets(options, args);
    addDryRun(options, args);
    addMonochrome(options, args);
    addTags(options, args);
    addPlugins(options, args);
    addFeatures(options, args);
    addGlue(options, args);
    addStrict(options, args);
    addJunitOptions(options, args);
  }

  private void addName(CucumberOptions options, List<String> args) {
    for (String name : options.name()) {
      args.add("--name");
      args.add(name);
    }
  }

  private void addSnippets(CucumberOptions options, List<String> args) {
    args.add("--snippets");
    args.add(options.snippets().toString());
  }

  private void addDryRun(CucumberOptions options, List<String> args) {
    if (options.dryRun()) {
      args.add("--dry-run");
    }
  }

  private void addMonochrome(CucumberOptions options, List<String> args) {
    if (options.monochrome() || runningInEnvironmentWithoutAnsiSupport()) {
      args.add("--monochrome");
    }
  }

  private void addTags(CucumberOptions options, List<String> args) {
    for (String tags : options.tags()) {
      args.add("--tags");
      args.add(tags);
    }
  }

  private void addPlugins(CucumberOptions options, List<String> args) {
    List<String> plugins = new ArrayList<>();
    plugins.addAll(asList(options.plugin()));
    for (String plugin : plugins) {
      args.add("--plugin");
      args.add(plugin);
    }
  }

  private void addFeatures(CucumberOptions options, List<String> args) {
    if (options != null && options.features().length != 0) {
      Collections.addAll(args, options.features());
      featuresSpecified = true;
    }
  }

  private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(List<String> args, Class clazz) {
    if (!featuresSpecified) {
      args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
    }
  }

  private void addGlue(CucumberOptions options, List<String> args) {
    boolean hasExtraGlue = options.extraGlue().length > 0;
    boolean hasGlue = options.glue().length > 0;

    if (hasExtraGlue && hasGlue) {
      throw new CucumberException("glue and extraGlue cannot be specified at the same time");
    }

    String[] gluePaths = {};
    if (hasExtraGlue) {
      gluePaths = options.extraGlue();
    }
    if (hasGlue) {
      gluePaths = options.glue();
      overridingGlueSpecified = true;
    }

    for (String glue : gluePaths) {
      args.add("--glue");
      args.add(glue);
    }
  }

  private void addDefaultGlueIfNoOverridingGlueIsSpecified(List<String> args, Class clazz) {
    if (!overridingGlueSpecified) {
      args.add("--glue");
      args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
    }
  }


  private void addStrict(CucumberOptions options, List<String> args) {
    if (options.strict()) {
      args.add("--strict");
    }
  }

  private void addJunitOptions(CucumberOptions options, List<String> args) {
    for (String junitOption : options.junit()) {
      args.add("--junit," + junitOption);
    }
  }

  static String packagePath(Class clazz) {
    return packagePath(packageName(clazz.getName()));
  }

  static String packagePath(String packageName) {
    return packageName.replace('.', '/');
  }

  static String packageName(String className) {
    return className.substring(0, Math.max(0, className.lastIndexOf(".")));
  }

  private boolean runningInEnvironmentWithoutAnsiSupport() {
    boolean intelliJidea = System.getProperty("idea.launcher.bin.path") != null;
    // TODO: What does Eclipse use?
    return intelliJidea;
  }

  private boolean hasSuperClass(Class classWithOptions) {
    return classWithOptions != Object.class;
  }

  private CucumberOptions getOptions(Class<?> clazz) {
    return clazz.getAnnotation(CucumberOptions.class);
  }
}
