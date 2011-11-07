package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Mappable;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTMLFormatter implements Formatter, Reporter {

    private final NiceAppendable out;
    private final String htmlReportDir = getHtmlReportDir();
    private BufferedWriter bufferedWriter;
    private boolean firstFeature = true;
    private int embeddedIndex;
    private static final String JS_FORMATTER_VAR = "formatter";
    private static final String JS_REPORT_FILENAME = "report.js";
    private static final String[] REPORT_ITEMS = new String[]{"formatter.js", "index.html", "jquery-1.6.4.min.js", "style.css"};
    private static final String CUCUMBER_HTMLREPORTERDIR = "cucumber.htmlreporterdir";
    private static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
        {
            put("image/bmp", "bmp");
            put("image/gif", "gif");
            put("image/jpeg", "jpg");
            put("image/png", "png");
        }
    };

    public HTMLFormatter() {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(htmlReportDir + JS_REPORT_FILENAME));
            out = new NiceAppendable(bufferedWriter);
        } catch (IOException e) {
            throw new CucumberException("Unable to create javascript report file: " + htmlReportDir
                    + JS_REPORT_FILENAME, e);
        }
    }

    @Override
    public void uri(String uri) {
        if (firstFeature) {
            out.append("$(document).ready(function() {").append("var ")
                    .append(JS_FORMATTER_VAR).append(" = new CucumberHTML.DOMFormatter($('.cucumber-report'));");
            firstFeature = false;
        }
        writeToJsReport("uri", "'" + uri + "'");
    }

    @Override
    public void feature(Feature feature) {
        writeToJsReport("feature", feature);
    }

    @Override
    public void background(Background background) {
        writeToJsReport("background", background);
    }

    @Override
    public void scenario(Scenario scenario) {
        writeToJsReport("scenario", scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        writeToJsReport("scenarioOutline", scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        writeToJsReport("examples", examples);
    }

    @Override
    public void step(Step step) {
        writeToJsReport("step", step);
    }

    @Override
    public void eof() {
        try {
            //TODO should do this stuff only after the last feature.
            out.append("});");
            bufferedWriter.close();
            copyReportFiles();
        } catch (IOException e) {

        }
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    @Override
    public void close() {
        // TODO - Close the HTML
        out.close();
    }

    private void writeToJsReport(String functionName, Mappable statement) {
        writeToJsReport(functionName, JSONValue.toJSONString(statement.toMap()).replaceAll("\\'", "\\\\'"));
    }

    private void writeToJsReport(String functionName, String arg) {
        out.append(JS_FORMATTER_VAR + ".").append(functionName).append("(").append(arg).append(");").println();
    }

    @Override
    public void result(Result result) {
        writeToJsReport("result", result);
    }

    @Override
    public void match(Match match) {
        writeToJsReport("match", match);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        // Creating a file instead of using data urls to not clutter the js file
        String extension = MIME_TYPES_EXTENSIONS.get(mimeType);
        if (extension != null) {
            StringBuilder fileName = new StringBuilder("embedded").append(embeddedIndex++).append(".")
                    .append(extension);
            copyResource(new ByteArrayInputStream(data), reportFileOutputStream(fileName.toString()));
            writeToJsReport("embedding", new StringBuilder("'").append(mimeType).append("','").append(fileName)
                    .append("'").toString());
        }
    }

    private void copyReportFiles() {
        String packageName = getClass().getPackage().getName().replaceAll("\\.", "/") + "/";
        InputStream resourceAsStream;
        for (String reportItem : REPORT_ITEMS) {
            resourceAsStream = getClass().getClassLoader().getResourceAsStream(packageName + reportItem);
            copyResource(resourceAsStream, reportFileOutputStream(reportItem));
        }
    }

    protected FileOutputStream reportFileOutputStream(String reportItem) {
        try {
            return new FileOutputStream(htmlReportDir + reportItem);
        } catch (FileNotFoundException e) {
            throw new CucumberException("Error creating html report file: " + htmlReportDir + reportItem, e);
        }
    }

    private void copyResource(InputStream in, FileOutputStream out) {
        byte[] buffer = new byte[16 * 1024];
        try {
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
            out.close();
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
    }

    private String getHtmlReportDir() {
        String htmlReportDir = System.getProperty(CUCUMBER_HTMLREPORTERDIR, "");
        if (!htmlReportDir.isEmpty()) {
            String fileSeparator = System.getProperty("file.separator");
            if (!htmlReportDir.endsWith(fileSeparator)) {
                htmlReportDir = htmlReportDir + fileSeparator;
            }
        }
        return htmlReportDir;
    }

}
