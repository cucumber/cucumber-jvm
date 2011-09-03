package cucumber.resources;

import cucumber.runtime.CucumberException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class FilePathExtractor {

    private Decoder decoder;

    public FilePathExtractor() {
        this(new Decoder());
    }

    public FilePathExtractor(Decoder decoder) {
        this.decoder = decoder;
    }

    public String filePath(String jarUrl) {
        String pathWithProtocol = jarUrl.substring(0, jarUrl.indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        // WINDOWS: jar:file:/C:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        // POSIX:   jar:file:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        String pathToJar = segments.length == 4 ? segments[2].substring(1) + ":" + segments[3] : segments[2];
        return resolveEncodedBlanksInPath(pathToJar);
    }

    public String resolveEncodedBlanksInPath(String path) {
        try {
            return decoder.decode(path);
        } catch (UnsupportedEncodingException e) {
            throw new CucumberException("UTF-8 is not supported on your system. This should not be happening.", e);
        }
    }
}