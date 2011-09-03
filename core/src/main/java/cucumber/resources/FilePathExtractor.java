package cucumber.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class FilePathExtractor {

    public String filePath(String jarUrl) throws UnsupportedEncodingException {
        String pathWithProtocol = jarUrl.substring(0, jarUrl.indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        // WINDOWS: jar:file:/C:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        // POSIX:   jar:file:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        String pathToJar = segments.length == 4 ? segments[2].substring(1) + ":" + segments[3] : segments[2];
        return URLDecoder.decode(pathToJar, "UTF-8");
    }

}