package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.MultiFormatter;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

public class RuntimeOptions {
    // TODO: Should be using the FormatterFactory for these.
    public static final String HTML_FORMATTER = "html";
    public static final String PROGRESS_FORMATTER = "progress";

    public static final String GLUE_REQUIRED = "Missing option: --glue";
    public static final String OUTPUT_REQUIRED = "Missing option: --out";
    public static final String USAGE = "TODO - Write the help"; // not sure if this really belongs here as it's CLI specific
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");

    private final List<String> _errors = new ArrayList<String>();
    private final List<String> _featurePaths = new ArrayList<String>();
    private final List<Object> _filters = new ArrayList<Object>(); // this feels like it should be List<String> and casted at the last moment
    private final List<String> _formats = new ArrayList<String>();
    private final List<String> _gluePaths = new ArrayList<String>();

    private String _dotCucumber;
    private String _outputPath;

    private boolean _dryRun;
    private boolean _helpRequested;
    private boolean _versionRequested;

    public RuntimeOptions() {
        reset();
    }

    public void parse(String[] $argv) {
        List<String> args = new ArrayList<String>(asList($argv));
        Iterator<String> iterator = args.iterator();

        while (iterator.hasNext()) {
            String arg = iterator.next();

            if (flagMatches(arg, "--help", "-h")) {
                setHelpRequired(true);
            } else if (flagMatches(arg, "--version", "-v")) {
                setVersionRequested(true);
            } else if (flagMatches(arg, "--glue", "-g")) {
                addGluePath(iterator.next());
            } else if (flagMatches(arg, "--tags", "-t")) {
                addFilterTag(iterator.next());
            } else if (flagMatches(arg, "--format", "-f")) {
                addFormat(iterator.next());
            } else if (flagMatches(arg, "--out", "-o")) {
                setOutputPath(iterator.next());
            } else if (flagMatches(arg, "--dotcucumber", "-c")) {
                setDotCucumber(iterator.next());
            } else if (flagMatches(arg, "--dry-run", "-d")) {
                setDryRun(true);
            } else {
                // TODO: Use PathWithLines and add line filter if any
                addFeaturePath(arg);
            }
        }
    }

    public String usage() {
        return USAGE;
    }

    public String version() {
        return VERSION;
    }

    public boolean hasErrors() {
        boolean hasErrors = false;
        getErrors().clear();

        if (getGluePaths().size() < 1) {
            hasErrors = true;
            getErrors().add(GLUE_REQUIRED);
        }

        if (getFormats().contains(HTML_FORMATTER) && getOutputPath().isEmpty()) {
            hasErrors = true;
            getErrors().add(OUTPUT_REQUIRED);
        }

        return hasErrors;
    }

    public List<String> getErrors() {
        return _errors;
    }

    public boolean flagMatches(String $flag, String $long, String $short) {
        return $flag.startsWith("-") && ($flag.equals($short) || $flag.equals($long));
    }

    public void reset() {
        _errors.clear();

        _featurePaths.clear();
        _filters.clear();
        _formats.clear();
        _gluePaths.clear();

        _dryRun = false;
        _helpRequested = false;
        _versionRequested = false;

        _dotCucumber = "";
        _outputPath = "";
    }

    public boolean isHelpRequested() {
        return _helpRequested;
    }

    public void setHelpRequired(boolean $helpRequired) {
        _helpRequested = $helpRequired;
    }

    public boolean isVersionRequested() {
        return _versionRequested;
    }

    public void setVersionRequested(boolean $versionRequested) {
        _versionRequested = $versionRequested;
    }

    public void setDryRun(boolean $dryRun) {
        _dryRun = $dryRun;
    }

    public boolean isDryRun() {
        return _dryRun;
    }

    public void setOutputPath(String $outputPath) {
        _outputPath = $outputPath;
    }

    public String getOutputPath() {
        return _outputPath;
    }

    public List<String> getFeaturePaths() {
        return _featurePaths;
    }

    public List<String> getGluePaths() {
        return _gluePaths;
    }

    public void setDotCucumber(String $dotCucumber) {
        _dotCucumber = $dotCucumber;
    }

    public void addGluePath(String $gluePath) {
        _gluePaths.add($gluePath);
    }

    public void addFeaturePath(String $featurePath) {
        _featurePaths.add($featurePath);
    }

    public String getDotCucumber() {
        return _dotCucumber;
    }

    public void addFilterTag(String $filter) {
        _filters.add($filter);
    }

    public List<Object> getFilterTags() {
        return _filters;
    }

    public List<String> getFormats() {
        return _formats;
    }

    public void addFormat(String $format) {
        _formats.add($format);
    }
}
