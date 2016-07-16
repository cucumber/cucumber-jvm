package cucumber.runtime.formatter;

import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HTMLFormatter implements Formatter {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String JS_FORMATTER_VAR = "formatter";
    private static final String JS_REPORT_FILENAME = "report.js";
    private static final String[] TEXT_ASSETS = new String[]{"/cucumber/formatter/formatter.js", "/cucumber/formatter/index.html", "/cucumber/formatter/jquery-1.8.2.min.js", "/cucumber/formatter/style.css"};
    private static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
        {
            put("image/bmp", "bmp");
            put("image/gif", "gif");
            put("image/jpeg", "jpg");
            put("image/png", "png");
            put("image/svg+xml", "svg");
            put("video/ogg", "ogg");
        }
    };

    private final URL htmlReportDir;
    private NiceAppendable jsOut;

    private boolean firstFeature = true;
    private int embeddedIndex;

    public HTMLFormatter(URL htmlReportDir) {
        this.htmlReportDir = htmlReportDir;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }

//    @Override
//    public void uri(String uri) {
//        if (firstFeature) {
//            jsOut().append("$(document).ready(function() {").append("var ")
//                    .append(JS_FORMATTER_VAR).append(" = new CucumberHTML.DOMFormatter($('.cucumber-report'));");
//            firstFeature = false;
//        }
//        jsFunctionCall("uri", uri);
//    }

/*    @Override
    public void feature(Feature feature) {
        jsFunctionCall("feature", feature);
    }

    @Override
    public void background(Background background) {
        jsFunctionCall("background", background);
    }

    @Override
    public void scenario(Scenario scenario) {
        jsFunctionCall("scenario", scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        jsFunctionCall("scenarioOutline", scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        jsFunctionCall("examples", examples);
    }

    @Override
    public void step(Step step) {
        jsFunctionCall("step", step);
    }*/

//    @Override
//    public void done() {
//        if (!firstFeature) {
//            jsOut().append("});");
//            copyReportFiles();
//        }
//    }
//
    public void close() {
        jsOut().close();
    }

/*    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void result(Result result) {
        jsFunctionCall("result", result);
    }

    @Override
    public void before(Match match, Result result) {
        jsFunctionCall("before", result);
    }

    @Override
    public void after(Match match, Result result) {
        jsFunctionCall("after", result);
    }

    @Override
    public void match(Match match) {
        jsFunctionCall("match", match);
    }*/

//    @Override
//    public void embedding(String mimeType, byte[] data) {
//        if(mimeType.startsWith("text/")) {
//            // just pass straight to the formatter to output in the html
//            jsFunctionCall("embedding", mimeType, new String(data));
//        } else {
//            // Creating a file instead of using data urls to not clutter the js file
//            String extension = MIME_TYPES_EXTENSIONS.get(mimeType);
//            if (extension != null) {
//                StringBuilder fileName = new StringBuilder("embedded").append(embeddedIndex++).append(".").append(extension);
//                writeBytesAndClose(data, reportFileOutputStream(fileName.toString()));
//                jsFunctionCall("embedding", mimeType, fileName);
//            }
//        }
//    }

//    @Override
//    public void write(String text) {
//        jsFunctionCall("write", text);
//    }

    private void jsFunctionCall(String functionName, Object... args) {
        NiceAppendable out = jsOut().append(JS_FORMATTER_VAR + ".").append(functionName).append("(");
        boolean comma = false;
        for (Object arg : args) {
            if (comma) {
                out.append(", ");
            }
            //arg = arg instanceof Mappable ? ((Mappable) arg).toMap() : arg;
            String stringArg = gson.toJson(arg);
            out.append(stringArg);
            comma = true;
        }
        out.append(");").println();
    }

    private void copyReportFiles() {
        for (String textAsset : TEXT_ASSETS) {
            InputStream textAssetStream = getClass().getResourceAsStream(textAsset);
            if (textAssetStream == null) {
                throw new CucumberException("Couldn't find " + textAsset + ". Is cucumber-html on your classpath? Make sure you have the right version.");
            }
            String baseName = new File(textAsset).getName();
            writeStreamAndClose(textAssetStream, reportFileOutputStream(baseName));
        }
    }

    private void writeStreamAndClose(InputStream in, OutputStream out) {
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

    private void writeBytesAndClose(byte[] buf, OutputStream out) {
        try {
            out.write(buf);
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
    }

    private NiceAppendable jsOut() {
        if (jsOut == null) {
            try {
                jsOut = new NiceAppendable(new OutputStreamWriter(reportFileOutputStream(JS_REPORT_FILENAME), "UTF-8"));
            } catch (IOException e) {
                throw new CucumberException(e);
            }
        }
        return jsOut;
    }

    private OutputStream reportFileOutputStream(String fileName) {
        try {
            return new URLOutputStream(new URL(htmlReportDir, fileName));
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

}