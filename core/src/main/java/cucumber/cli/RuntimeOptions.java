package cucumber.cli;

import java.util.*;
import static java.util.Arrays.asList;

public class RuntimeOptions {
    // TODO: Should be using the FormatterFactory for these.
    public static final String HTML_FORMATTER = "html";
    public static final String PROGRESS_FORMATTER = "progress";
    public static final String SEPARATOR = System.getProperty("line.separator");

    public static final String GLUE_REQUIRED = "Missing option: --glue";
    public static final String OUTPUT_REQUIRED = "Missing option: --out";
    public static final String THERE_CAN_ONLY_BE_ONE = "Only one format can be used with stdout, missing option(s): --out";
    public static final String USAGE = "TODO - Write the help"; // not sure if this really belongs here as it's CLI specific
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");

    private final List<String> _errors = new ArrayList<String>();
    private final List<String> _featurePaths = new ArrayList<String>();
    private final List<Object> _filterTags = new ArrayList<Object>(); // this feels like it should be List<String> and casted at the last moment
    private final List<String> _formats = new ArrayList<String>();
    private final List<String> _gluePaths = new ArrayList<String>();
    private final Map<String, String> _outputPaths = new HashMap<String, String>();

    private String _dotCucumber;
    private String _currentFormat;

    private boolean _dryRun;
    private boolean _helpRequested;
    private boolean _versionRequested;

    public RuntimeOptions() {
        reset();
    }

    public interface Formatable {
        public void each(String $format, String $destination);
    }

    public interface Filterable {
        public void each(Object $filterTag);
    }

    public void applyErrors(Messagable $stringable) {
        // TODO: Will likely want to rejig this later so individual fields could be h
        for(String error : _errors) {
            $stringable.message(error);
        }
    }

    public void applyFeaturePaths(Messagable $messagable) {
        for(String path : _featurePaths) {
            $messagable.message(path);
        }
    }
    
    public void applyFilterTags(Filterable $filterable) {
        for(Object filter : getFilterTags()) {
            $filterable.each(filter);
        }
    }
    
    public void applyFormats(Formatable $formatable) {
        for(String format : getFormats()) {
            $formatable.each(format, getOutputPath(format));
        }
    }
    
    public void applyGluePaths(Messagable $messagable) {
        for(String gluePath : getGluePaths()) {
            $messagable.message(gluePath);
        }
    }

    public void applyHelpRequested(Messagable $messagable) {
        if(_helpRequested) $messagable.message(USAGE);
    }

    public void applyVersionRequested(Messagable $messagable) {
        if(_versionRequested) $messagable.message(VERSION);
    }

    public void parse(String[] $argv) {
        List<String> args = new ArrayList<String>(asList($argv));
        Iterator<String> iterator = args.iterator();

        while (iterator.hasNext()) {
            String arg = iterator.next();

            if (flagMatches(arg, "--help", "-h")) {
                setHelpRequested(true);
            } else if (flagMatches(arg, "--version", "-v")) {
                setVersionRequested(true);
            } else if (flagMatches(arg, "--glue", "-g")) {
                addGluePath(iterator.next());
            } else if (flagMatches(arg, "--tags", "-t")) {
                addFilterTag(iterator.next());
            } else if (flagMatches(arg, "--format", "-f")) {
                addFormat(iterator.next());
            } else if (flagMatches(arg, "--out", "-o")) {
                addOutputPath(iterator.next());
            } else if (flagMatches(arg, "--dotcucumber", "-c")) {
                setDotCucumber(iterator.next());
            } else if (flagMatches(arg, "--dry-run", "-d")) {
                setDryRun(true);
            } else {
                // TODO: Use PathWithLines and add line filter if any
                addFeaturePath(arg);
            }
        }

        validate();
    }

    public void validate() {
        _errors.clear();

        if (_gluePaths.size() < 1) {
            _errors.add(GLUE_REQUIRED);
        }

        if (getFormats().contains(HTML_FORMATTER) && getOutputPath("html") == null) {
            _errors.add(OUTPUT_REQUIRED);
        }

        if (_formats.size() - _outputPaths.size() > 1) {
            _errors.add(THERE_CAN_ONLY_BE_ONE);
        }
    }

    public boolean flagMatches(String $flag, String $long, String $short) {
        return $flag.startsWith("-") && ($flag.equals($short) || $flag.equals($long));
    }

    protected void reset() {
        _errors.clear();

        _featurePaths.clear();
        _filterTags.clear();
        _formats.clear();
        _gluePaths.clear();
        _outputPaths.clear();
        _dryRun = false;
        _helpRequested = false;
        _versionRequested = false;

        _dotCucumber = "";
        _currentFormat = "";
    }

    public void setHelpRequested(boolean $helpRequested) {
        _helpRequested = $helpRequested;
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

    // TODO: remove the getters as it's bleeding state
    protected List<String> getFeaturePaths() {
        return _featurePaths;
    }

    protected List<String> getGluePaths() {
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
        _filterTags.add($filter);
    }

    protected List<Object> getFilterTags() {
        return _filterTags;
    }

    protected List<String> getFormats() {
        return _formats;
    }

    public void addFormat(String $format) {
        _formats.add($format);
        _currentFormat = $format;
    }

    public void addOutputPath(String $outputPath) {
        _outputPaths.put(_currentFormat, $outputPath);
        _currentFormat = "";
    }

    public String getOutputPath(String $format) {
        return _outputPaths.get($format);
    }

    protected Map<String, String> getOutputPaths() {
        return _outputPaths;
    }
}
