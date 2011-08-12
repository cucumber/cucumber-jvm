package cucumber.cli;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class CliArgs {
    @Parameter(description = "Files or Directories")
    public List<String> filesOrDirs = new ArrayList<String>();
    
    @Parameter(names = { "--glue", "-g" }, description = "Package Name or Script Prefix to scan for Implementations")
    public String packageNameOrScriptPrefix;

    @Parameter(names = { "--version", "-v" }, description = "Show Cucumber Version")
    public boolean showVersion;
    
    @Parameter(names = { "--help", "-h" }, description = "Show Help")
    public boolean showUsage;
    
}
