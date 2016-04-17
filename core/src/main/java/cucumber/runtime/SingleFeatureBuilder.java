package cucumber.runtime;

import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.FilterFormatter;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import gherkin.lexer.Encoding;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleFeatureBuilder implements Formatter {
    private final char fileSeparatorChar;
    private final MessageDigest md5;
    private final Map<String, String> pathsByChecksum = new HashMap<String, String>();
    CucumberFeature currentCucumberFeature;
    private String featurePath;
    public static final String UTF_8="UTF-8";

    SingleFeatureBuilder(List<CucumberFeature> cucumberFeatures, char fileSeparatorChar) {
        this(fileSeparatorChar);
    }

    public SingleFeatureBuilder() {
        this(File.separatorChar);
    }

    public SingleFeatureBuilder(char fileSeparatorChar) {
        this.fileSeparatorChar = fileSeparatorChar;
        try {
            this.md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void uri(String uri) {
        this.featurePath = uri;
    }

    @Override
    public void feature(Feature feature) {
        currentCucumberFeature = new CucumberFeature(feature, featurePath);
    }

    @Override
    public void background(Background background) {
        currentCucumberFeature.background(background);
    }

    @Override
    public void scenario(Scenario scenario) {
        currentCucumberFeature.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        currentCucumberFeature.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        currentCucumberFeature.examples(examples);
    }

    @Override
    public void step(Step step) {
        currentCucumberFeature.step(step);
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void done() {
    }

    @Override
    public void close() {
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    public void parse(Resource resource, List<Object> filters) {
        String gherkin = read(resource);

        String checksum = checksum(gherkin);
        String path = pathsByChecksum.get(checksum);
        if (path != null) {
            return;
        }
        pathsByChecksum.put(checksum, resource.getPath());

        Formatter formatter = this;
        if (!filters.isEmpty()) {
            formatter = new FilterFormatter(this, filters);
        }
        Parser parser = new Parser(formatter);

        try {
            parser.parse(gherkin, convertFileSeparatorToForwardSlash(resource.getPath()), 0);
        } catch (Exception e) {
            throw new CucumberException(String.format("Error parsing feature file %s", convertFileSeparatorToForwardSlash(resource.getPath())), e);
        }
        I18n i18n = parser.getI18nLanguage();
        if (currentCucumberFeature != null) {
            // The current feature may be null if we used a very restrictive filter, say a tag that isn't used.
            // Might also happen if the feature file itself is empty.
            currentCucumberFeature.setI18n(i18n);
        }
    }

    private String convertFileSeparatorToForwardSlash(String path) {
        return path.replace(fileSeparatorChar, '/');
    }

    private String checksum(String gherkin) {
        return new BigInteger(1, md5.digest(gherkin.getBytes(Charset.forName(UTF_8)))).toString(16);
    }

    public static String read(Resource resource) {
        InputStreamReader utf8Reader = null;
        InputStreamReader specificEncodingReader = null;

        try {
            utf8Reader = new InputStreamReader(resource.getInputStream(), UTF_8);
            String source = FixJava.readReader(utf8Reader);
            String encoding = new Encoding().encoding(source);
            if (!UTF_8.equals(encoding)) {
                specificEncodingReader = new InputStreamReader(resource.getInputStream(), encoding);
                source = FixJava.readReader(specificEncodingReader);
            }
            return source;
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        } finally{
            try {
                if (utf8Reader != null) {
                    utf8Reader.close();
                }
                if (specificEncodingReader != null) {
                    specificEncodingReader.close();
                }
            } catch (IOException ioe) {
                throw new CucumberException("Failed to close InputStreamReader reading resource:" + resource.getPath(), ioe);
            }
        }
    }

    public CucumberFeature getCurrentCucumberFeature() {
        return currentCucumberFeature;
    }
}
