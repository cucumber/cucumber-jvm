package cucumber.formatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.runtime.CucumberException;
import gherkin.deps.org.json.simple.JSONValue;
import gherkin.formatter.Formatter;
import gherkin.formatter.Mappable;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTMLFormatter implements Formatter, Reporter {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String JS_FORMATTER_VAR = "formatter";
    private static final String JS_REPORT_FILENAME = "report.js";
    private static final String[] REPORT_ITEMS = new String[]{"formatter.js", "index.html", "jquery-1.6.4.min.js", "style.css"};
    private static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
        {
            put("image/bmp", "bmp");
            put("image/gif", "gif");
            put("image/jpeg", "jpg");
            put("image/png", "png");
        }
    };

    private final File htmlReportDir;
    private NiceAppendable jsOut;

    private boolean firstFeature = true;
    private int embeddedIndex;

    public HTMLFormatter(File htmlReportDir) {
        this.htmlReportDir = htmlReportDir;
    }

    @Override
    public void uri(String uri) {
        if (firstFeature) {
            jsOut().append("$(document).ready(function() {").append("var ")
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
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    @Override
    public void done() {
        jsOut().append("});");
        jsOut().close();
        copyReportFiles();
    }

    private void writeToJsReport(String functionName, Mappable statement) {
        String json = gson.toJson(statement.toMap());
        writeToJsReport(functionName, json);
    }

    private void writeToJsReport(String functionName, String arg) {
        jsOut().append(JS_FORMATTER_VAR + ".").append(functionName).append("(").append(arg).append(");").println();
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
            StringBuilder fileName = new StringBuilder("embedded").append(embeddedIndex++).append(".").append(extension);
            writeBytes(new ByteArrayInputStream(data), reportFileOutputStream(fileName.toString()));
            writeToJsReport("embedding", new StringBuilder("'").append(mimeType).append("','").append(fileName).append("'").toString());
        }
    }

    private void copyReportFiles() {
        String packageName = getClass().getPackage().getName().replaceAll("\\.", "/") + "/";
        InputStream resourceAsStream;
        for (String reportItem : REPORT_ITEMS) {
            resourceAsStream = getClass().getClassLoader().getResourceAsStream(packageName + reportItem);
            writeBytes(resourceAsStream, reportFileOutputStream(reportItem));
        }
    }

    private void writeBytes(InputStream in, OutputStream out) {
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

    private NiceAppendable jsOut() {
        if (jsOut == null) {
            try {
                jsOut = new NiceAppendable(new OutputStreamWriter(reportFileOutputStream(JS_REPORT_FILENAME), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new CucumberException(e);
            }
        }
        return jsOut;
    }

    private OutputStream reportFileOutputStream(String fileName) {
        File file = new File(htmlReportDir, fileName);
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new CucumberException("Error creating file: " + file.getAbsolutePath(), e);
        }
    }

}
