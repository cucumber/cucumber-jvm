package cucumber.cli;

import java.util.*;
import static java.util.Arrays.asList;

public class RuntimeOptions {
    public static final String GLUE_REQUIRED = "Missing option: --glue";
    public static final String OUTPUT_REQUIRED = "Missing option: --out";
    public static final String THERE_CAN_ONLY_BE_ONE = "Only one format can be used with stdout, missing option(s): --out";
    public static final String USAGE = "TODO - Write the help"; // not sure if this really belongs here as it's CLI specific
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");

    private final List<String> errors = new ArrayList<String>();
    private final List<String> featurePaths = new ArrayList<String>();
    private final List<Object> filterTags = new ArrayList<Object>(); // this feels like it should be List<String> and casted at the last moment
    private final List<String> formats = new ArrayList<String>();
    private final List<String> gluePaths = new ArrayList<String>();
    private final Map<String, String> outputPaths = new HashMap<String, String>();

    private String dotCucumber;
    private String currentFormat;

    private boolean dryRun;
    private boolean helpRequested;
    private boolean versionRequested;

    public RuntimeOptions() {
        reset();
    }

    public void applyErrorsTo(Messagable stringable) {
        // TODO: Will likely want to rejig this later so individual fields could be h
        for(String error : errors) {
            stringable.message(error);
        }
    }

    public void applyFeaturePaths(Messagable messagable) {
        for(String path : featurePaths) {
            messagable.message(path);
        }
    }
    
    public void applyFilterTags(Filterable filterable) {
        for(Object filter : getFilterTags()) {
            filterable.each(filter);
        }
    }
    
    public void applyFormats(Formatable formatable) {
        for(String format : formats) {
            formatable.eachWithDestination(format, outputPaths.get(format));
        }
    }
    
    public void applyGluePaths(Messagable messagable) {
        for(String gluePath : gluePaths) {
            messagable.message(gluePath);
        }
    }

    public void applyIfHelpRequestedTo(Messagable messagable) {
        if(helpRequested) messagable.message(USAGE);
    }

    public void applyIfVersionRequestedTo(Messagable messagable) {
        if(versionRequested) messagable.message(VERSION);
    }

    public void parse(String[] argv) {
        List<String> args = new ArrayList<String>(asList(argv));
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
        errors.clear();

        if (gluePaths.size() < 1) {
            errors.add(GLUE_REQUIRED);
        }

        if (formats.size() - outputPaths.size() > 1) {
            errors.add(THERE_CAN_ONLY_BE_ONE);
        }
    }

    public boolean flagMatches(String flag, String longFlag, String shortFlag) {
        return flag.startsWith("-") && (flag.equals(shortFlag) || flag.equals(longFlag));
    }

    protected void reset() {
        errors.clear();

        featurePaths.clear();
        filterTags.clear();
        formats.clear();
        gluePaths.clear();
        outputPaths.clear();

        dryRun = false;
        helpRequested = false;
        versionRequested = false;

        dotCucumber = "";
        currentFormat = "";
    }

    public void setHelpRequested(boolean helpRequested) {
        this.helpRequested = helpRequested;
    }

    public void setVersionRequested(boolean versionRequested) {
        this.versionRequested = versionRequested;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setDotCucumber(String dotCucumber) {
        this.dotCucumber = dotCucumber;
    }

    public void addGluePath(String gluePath) {
        gluePaths.add(gluePath);
    }

    public void addFeaturePath(String featurePath) {
        featurePaths.add(featurePath);
    }

    public void addFilterTag(String filter) {
        filterTags.add(filter);
    }

    public void addFormat(String format) {
        formats.add(format);
        currentFormat = format;
    }

    public void addOutputPath(String outputPath) {
        outputPaths.put(currentFormat, outputPath);
        currentFormat = "";
    }

    // TODO: remove the getters as it's bleeding state
    protected List<String> getFeaturePaths() {
        return featurePaths;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    protected List<String> getGluePaths() {
        return gluePaths;
    }

    protected Map<String, String> getOutputPaths() {
        return outputPaths;
    }

    protected String getOutputPath(String format) {
        return outputPaths.get(format);
    }

    protected List<String> getFormats() {
        return formats;
    }

    protected List<Object> getFilterTags() {
        return filterTags;
    }

    public String getDotCucumber() {
        return dotCucumber;
    }
}
