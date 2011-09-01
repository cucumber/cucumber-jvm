package cucumber.resources;

public class FilePathExtractor {

    public static String filePath(String jarUrl) {
        String pathWithProtocol = jarUrl.substring(0, jarUrl.indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        // WINDOWS: jar:file:/C:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        // POSIX:   jar:file:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        String pathToJar = segments.length == 4 ? segments[2].substring(1) + ":" + segments[3] : segments[2];
        return resolveEncodedBlanksInPath(pathToJar);
    }

    public static String resolveEncodedBlanksInPath(String path) {
        return path.replaceAll("%20", " ");
    }
}