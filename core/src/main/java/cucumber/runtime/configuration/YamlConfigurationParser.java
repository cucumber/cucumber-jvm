package cucumber.runtime.configuration;

import cucumber.runtime.ConfigurationParser;
import java.io.Reader;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class YamlConfigurationParser implements ConfigurationParser {

  private Reader reader;

  public YamlConfigurationParser(Reader reader) {
    this.reader = reader;
  }

  @Override
  public Map<String, ?> getMap() {
    return new Yaml().load(reader);
  }

}
