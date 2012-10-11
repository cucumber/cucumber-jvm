package cucumber.runtime;

import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.FilterFormatter;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.lexer.Encoding;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureBuilder implements Formatter {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final List<CucumberFeature> cucumberFeatures;
    private final MessageDigest md5;
    private final Map<String, String> pathsByChecksum = new HashMap<String, String>();
    private CucumberFeature currentCucumberFeature;
    private String uri;

    public FeatureBuilder(List<CucumberFeature> cucumberFeatures) {
        this.cucumberFeatures = cucumberFeatures;
        try {
            this.md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void uri(String uri) {
        this.uri = uri;
    }

    @Override
    public void feature(Feature feature) {
        currentCucumberFeature = new CucumberFeature(feature, uri);
        cucumberFeatures.add(currentCucumberFeature);
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

        parser.parse(gherkin, resource.getPath(), 0);
        I18n i18n = parser.getI18nLanguage();
        if (currentCucumberFeature != null) {
            // The current feature may be null if we used a very restrictive filter, say a tag that isn't used.
            // Might also happen if the feature file itself is empty.
            currentCucumberFeature.setI18n(i18n);
        }
    }

    private String checksum(String gherkin) {
        return new BigInteger(1, md5.digest(gherkin.getBytes(UTF8))).toString(16);
    }

    private String read(Resource resource) {
        try {
            String source = FixJava.readReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
            String encoding = new Encoding().encoding(source);
            if (!"UTF-8".equals(encoding)) {
                source = FixJava.readReader(new InputStreamReader(resource.getInputStream(), encoding));
            }
            return source;
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }
}
