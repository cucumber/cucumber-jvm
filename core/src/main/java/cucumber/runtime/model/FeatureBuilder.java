package cucumber.runtime.model;

import cucumber.runtime.CucumberException;
import cucumber.runtime.io.Resource;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureBuilder {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final List<CucumberFeature> cucumberFeatures = new ArrayList<>();
    private final MessageDigest md;
    private final Set<String> pathsByChecksum = new HashSet<>();

    public FeatureBuilder() {
        try {
            this.md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new CucumberException(e);
        }
    }

    public List<CucumberFeature> build() {
        Collections.sort(cucumberFeatures, new CucumberFeature.CucumberFeatureUriComparator());
        return cucumberFeatures;
    }

    public void parse(Resource resource) {
        CucumberFeature feature = FeatureParser.parseResource(resource);

        String checksum = checksum(feature.getSource());
        if (pathsByChecksum.contains(checksum)) {
            return;
        }
        pathsByChecksum.add(checksum);
        cucumberFeatures.add(feature);

    }

    private String checksum(String gherkin) {
        return new BigInteger(1, md.digest(gherkin.getBytes(UTF8))).toString(16);
    }


}
