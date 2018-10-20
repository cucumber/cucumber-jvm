package cucumber.runtime.configuration;

import static java.util.Arrays.asList;
import cucumber.runtime.ConfigurationParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionsConfigurationParser implements ConfigurationParser {

  List<String> args;

  public OptionsConfigurationParser(List<String> args) {
    this.args = args;
  }

  @Override
  public Map<String, ?> getMap() {
    Map<String, Object> map = new HashMap<String, Object>();
    while (!args.isEmpty()) {
      String arg = args.remove(0).trim();

      if (arg.equals("--threads")) {
        String threads = args.remove(0);
        map.put("threads", Integer.valueOf(threads));
      } else if (arg.equals("--glue") || arg.equals("-g")) {
        addToList(map, "glue", args.remove(0));
      } else if (arg.equals("--tags") || arg.equals("-t")) {
        addToList(map, "tags", args.remove(0));
      } else if (arg.equals("--plugin") || arg.equals("--add-plugin") || arg.equals("-p")) {
        addToList(map, "plugins", args.remove(0));
      } else if (arg.equals("--no-dry-run") || arg.equals("--dry-run") || arg.equals("-d")) {
        map.put("dryRun", !arg.startsWith("--no-"));
      } else if (arg.equals("--no-strict") || arg.equals("--strict") || arg.equals("-s")) {
        map.put("strict", !arg.startsWith("--no-"));
      } else if (arg.equals("--no-monochrome") || arg.equals("--monochrome") || arg.equals("-m")) {
        map.put("monochrome", !arg.startsWith("--no-"));
      } else if (arg.equals("--snippets")) {
        map.put("snippets", args.remove(0));
      } else if (arg.equals("--name") || arg.equals("-n")) {
        map.put("name", args.remove(0));
      } else if (arg.startsWith("--junit,")) {
        for (String junit : arg.substring("--junit,".length()).split(",")) {
          addToList(map, "junit", junit);
        }
      } else if (arg.equals("--wip") || arg.equals("-w")) {
        map.put("wip", true);
      } else {
        map.put("features", arg);
      }
    }
    return map;
  }

  private void addToList(Map<String, Object> map, String key, String value) {
    // TODO: add some checks
    List list = (List)map.get(key);
    if (list == null) {
      list = new ArrayList();
    }
    list.add(value);
    map.put(key, list);
  }
}
