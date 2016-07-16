package cucumber.runtime;

import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import cucumber.util.Encoding;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureBuilder {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final List<CucumberFeature> cucumberFeatures;
    private final char fileSeparatorChar;
    private final MessageDigest md5;
    private final Map<String, String> pathsByChecksum = new HashMap<String, String>();

    public FeatureBuilder(List<CucumberFeature> cucumberFeatures) {
        this(cucumberFeatures, File.separatorChar);
    }

    FeatureBuilder(List<CucumberFeature> cucumberFeatures, char fileSeparatorChar) {
        this.cucumberFeatures = cucumberFeatures;
        this.fileSeparatorChar = fileSeparatorChar;
        try {
            this.md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CucumberException(e);
        }
    }

    public void parse(Resource resource) {
        String gherkin = read(resource);

        String checksum = checksum(gherkin);
        String path = pathsByChecksum.get(checksum);
        if (path != null) {
            return;
        }
        pathsByChecksum.put(checksum, resource.getPath());

        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();
        try {
            GherkinDocument gherkinDocument = parser.parse(gherkin, matcher);
            CucumberFeature feature = new CucumberFeature(gherkinDocument, convertFileSeparatorToForwardSlash(resource.getPath()), gherkin);
            cucumberFeatures.add(feature);
        } catch (ParserException e) {
            throw new CucumberException(e);
        }
     }

    private String convertFileSeparatorToForwardSlash(String path) {
        return path.replace(fileSeparatorChar, '/');
    }

    private String checksum(String gherkin) {
        return new BigInteger(1, md5.digest(gherkin.getBytes(UTF8))).toString(16);
    }

    public String read(Resource resource) {
        try {
            String source = Encoding.readFile(resource);
            return source;
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }
}
