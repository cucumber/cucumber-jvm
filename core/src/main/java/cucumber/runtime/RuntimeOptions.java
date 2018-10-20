package cucumber.runtime;

import cucumber.api.SnippetType;
import cucumber.runtime.configuration.OptionsConfigurationParser;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.model.PathWithLines;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

// IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
public class RuntimeOptions {
  public static final String VERSION =
      ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
  public static final String USAGE_RESOURCE = "/cucumber/api/cli/USAGE.txt";

  static String usageText;

  private final List<String> glue = new ArrayList<String>();
  private final List<String> tagFilters = new ArrayList<String>();
  private final List<Pattern> nameFilters = new ArrayList<Pattern>();
  private final Map<String, List<Long>> lineFilters = new HashMap<String, List<Long>>();
  private final List<String> featurePaths = new ArrayList<String>();

  private final List<String> junitOptions = new ArrayList<String>();
  private boolean dryRun;
  private boolean strict = false;
  private boolean monochrome = false;
  private boolean wip = false;
  private SnippetType snippetType = SnippetType.UNDERSCORE;
  private int threads = 1;

  private final List<String> pluginFormatterNames = new ArrayList<String>();
  private final List<String> pluginStepDefinitionReporterNames = new ArrayList<String>();
  private final List<String> pluginSummaryPrinterNames = new ArrayList<String>();


  /**
   * Create a new instance from a string of options, for example:
   * <p/>
   * <pre<{@code "--name 'the fox' --plugin pretty --strict"}
   * </pre>
   *
   * @param argv the arguments
   */
  public RuntimeOptions(String argv) {
    this(Shellwords.parse(argv));
  }

  public RuntimeOptions(Map<String, ?> map) {
    parse(map);
  }

  /**
   * Create a new instance from a list of options, for example:
   * <p/>
   * <pre<{@code Arrays.asList("--name", "the fox", "--plugin", "pretty", "--strict");}
   * </pre>
   *
   * @param argv the arguments
   */
  public RuntimeOptions(List<String> argv) {
    this(Env.INSTANCE, argv);
  }

  public RuntimeOptions(Env env, List<String> argv) {
    argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
    parse(new OptionsConfigurationParser(argv).getMap());

    String cucumberOptionsFromEnv = env.get("cucumber.options");
    if (cucumberOptionsFromEnv != null) {
      parse(new OptionsConfigurationParser(Shellwords.parse(cucumberOptionsFromEnv)).getMap());
    }

    if (pluginFormatterNames.isEmpty()) {
      pluginFormatterNames.add("progress");
    }
    if (pluginSummaryPrinterNames.isEmpty()) {
      pluginSummaryPrinterNames.add("default_summary");
    }
  }

  public boolean isMultiThreaded() {
    return threads > 1;
  }

  public RuntimeOptions noSummaryPrinter() {
    pluginSummaryPrinterNames.clear();
    return this;
  }

  public List<String> getPluginFormatterNames() {
    return pluginFormatterNames;
  }

  public List<String> getPluginSummaryPrinterNames() {
    return pluginSummaryPrinterNames;
  }

  public List<String> getPluginStepDefinitionReporterNames() {
    return pluginStepDefinitionReporterNames;
  }

  private <T> List<T> getList(Map<String, ?> map, String key, Class<T> clazz) {
    Object object = getValue(map, key, Object.class);
    List<T> list = null;
    if (object != null) {
      if (object instanceof List) {
        list = (List) object;
      } else { // single element
        list = new ArrayList<T>();
        if (!clazz.isAssignableFrom(object.getClass())) {
          throw new IllegalArgumentException(String.format(
              "Parameter %s is not of expected type %s", object, clazz.getName()));
        }
        list.add((T) object);
      }
      for (Object element : list) {
        if (!clazz.isAssignableFrom(element.getClass())) {
          throw new IllegalArgumentException(String.format(
              "Parameter %s is not of expected type %s", element, clazz.getName()));
        }
      }
    }
    if (list == null) {
      list = Collections.emptyList();
    }
    return (List<T>) list;
  }

  private List<String> getList(Map<String, ?> map, String key) {
    return getList(map, key, String.class);
  }

  private <T> T getValue(Map<String, ?> map, String key, Class<T> clazz) {
    try {
      return clazz.cast(map.get(key));
    } catch (ClassCastException exception) {
      throw new IllegalArgumentException(String.format("Parameter %s is not of expected type %s",
          key, clazz.getName()));
    }
  }

  private String getValue(Map<String, ?> map, String key) {
    return getValue(map, key, String.class);
  }

  private int getInt(Map<String, ?> map, String key, int defaultValue) {
    Integer intValue = getValue(map, key, Integer.class);
    return (intValue != null) ? intValue : defaultValue;
  }

  private boolean getBoolean(Map<String, ?> map, String key, boolean defaultValue) {
    Boolean booleanValue = getValue(map, key, Boolean.class);
    return (booleanValue != null) ? booleanValue : defaultValue;
  }

  private void parse(Map<String, ?> map) {
    List<Pattern> parsedNameFilters = new ArrayList<Pattern>();
    Map<String, List<Long>> parsedLineFilters = new HashMap<String, List<Long>>();
    List<String> parsedFeaturePaths = new ArrayList<String>();
    List<String> parsedJunitOptions = new ArrayList<String>();

    // thread
    this.threads = getInt(map, "threads", 1);
    if (this.threads < 1) {
      throw new CucumberException("--threads must be > 0");
    }

    // glue
    List<String> parsedGlue = getList(map, "glue");

    // tags
    List<String> parsedTagFilters = getList(map, "tags");

    // plugins
    ParsedPluginData parsedPluginData = new ParsedPluginData();
    for (String plugin : getList(map, "plugins")) {
      parsedPluginData.addPluginName(plugin, true);
    }

    // dry-run
    dryRun = getBoolean(map, "dryRun", false);

    // strict
    strict = getBoolean(map, "strict", false);

    // monochrome
    monochrome = getBoolean(map, "monochrome", false);

    // snippets
    String snippets = getValue(map, "snippets");
    snippetType = (snippets != null) ? SnippetType.fromString(snippets) : SnippetType.CAMELCASE;

    // name
    List<String> names = getList(map, "name");
    for (String pattern : names) {
      Pattern patternFilter = Pattern.compile(pattern);
      parsedNameFilters.add(patternFilter);
    }

    // junit
    parsedJunitOptions = getList(map, "junit");

    // wip
    wip = getBoolean(map, "wip", false);

    // features
    String features = getValue(map, "features");
    if (features != null && !features.isEmpty()) {
      PathWithLines pathWithLines = new PathWithLines(getValue(map, "features"));
      parsedFeaturePaths.add(pathWithLines.path);
      if (!pathWithLines.lines.isEmpty()) {
        String key = pathWithLines.path.replace("classpath:", "");
        addLineFilters(parsedLineFilters, key, pathWithLines.lines);
      }
      if (!parsedTagFilters.isEmpty() || !parsedNameFilters.isEmpty()
          || !parsedLineFilters.isEmpty() || haveLineFilters(parsedFeaturePaths)) {
        tagFilters.clear();
        tagFilters.addAll(parsedTagFilters);
        nameFilters.clear();
        nameFilters.addAll(parsedNameFilters);
        lineFilters.clear();
        for (String path : parsedLineFilters.keySet()) {
          lineFilters.put(path, parsedLineFilters.get(path));
        }
      }
      if (!parsedFeaturePaths.isEmpty()) {
        featurePaths.clear();
        featurePaths.addAll(parsedFeaturePaths);
      }
    }

    if (!parsedGlue.isEmpty()) {
      glue.clear();
      glue.addAll(parsedGlue);
    }
    if (!parsedJunitOptions.isEmpty()) {
      junitOptions.clear();
      junitOptions.addAll(parsedJunitOptions);
    }

    parsedPluginData.updatePluginFormatterNames(pluginFormatterNames);
    parsedPluginData.updatePluginStepDefinitionReporterNames(pluginStepDefinitionReporterNames);
    parsedPluginData.updatePluginSummaryPrinterNames(pluginSummaryPrinterNames);
  }

  private void addLineFilters(Map<String, List<Long>> parsedLineFilters, String key,
      List<Long> lines) {
    if (parsedLineFilters.containsKey(key)) {
      parsedLineFilters.get(key).addAll(lines);
    } else {
      parsedLineFilters.put(key, lines);
    }
  }

  private boolean haveLineFilters(List<String> parsedFeaturePaths) {
    for (String pathName : parsedFeaturePaths) {
      if (pathName.startsWith("@") || PathWithLines.hasLineFilters(pathName)) {
        return true;
      }
    }
    return false;
  }

  public List<String> getGlue() {
    return glue;
  }

  public boolean isStrict() {
    return strict;
  }

  public boolean isDryRun() {
    return dryRun;
  }

  public boolean isWip() {
    return wip;
  }

  public List<String> getFeaturePaths() {
    return featurePaths;
  }

  public List<Pattern> getNameFilters() {
    return nameFilters;
  }

  public List<String> getTagFilters() {
    return tagFilters;
  }

  public Map<String, List<Long>> getLineFilters() {
    return lineFilters;
  }

  public boolean isMonochrome() {
    return monochrome;
  }

  public SnippetType getSnippetType() {
    return snippetType;
  }

  public List<String> getJunitOptions() {
    return junitOptions;
  }

  public int getThreads() {
    return threads;
  }

  class ParsedPluginData {
    ParsedOptionNames formatterNames = new ParsedOptionNames();
    ParsedOptionNames stepDefinitionReporterNames = new ParsedOptionNames();
    ParsedOptionNames summaryPrinterNames = new ParsedOptionNames();

    public void addPluginName(String name, boolean isAddPlugin) {
      if (PluginFactory.isStepDefinitionReporterName(name)) {
        stepDefinitionReporterNames.addName(name, isAddPlugin);
      } else if (PluginFactory.isSummaryPrinterName(name)) {
        summaryPrinterNames.addName(name, isAddPlugin);
      } else if (PluginFactory.isFormatterName(name)) {
        formatterNames.addName(name, isAddPlugin);
      } else {
        throw new CucumberException("Unrecognized plugin: " + name);
      }
    }

    public void updatePluginFormatterNames(List<String> pluginFormatterNames) {
      formatterNames.updateNameList(pluginFormatterNames);
    }

    public void updatePluginStepDefinitionReporterNames(
        List<String> pluginStepDefinitionReporterNames) {
      stepDefinitionReporterNames.updateNameList(pluginStepDefinitionReporterNames);
    }

    public void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
      summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
    }
  }

  class ParsedOptionNames {
    private List<String> names = new ArrayList<String>();
    private boolean clobber = false;

    public void addName(String name, boolean isAddOption) {
      names.add(name);
      if (!isAddOption) {
        clobber = true;
      }
    }

    public void updateNameList(List<String> nameList) {
      if (!names.isEmpty()) {
        if (clobber) {
          nameList.clear();
        }
        nameList.addAll(names);
      }
    }
  }

}
