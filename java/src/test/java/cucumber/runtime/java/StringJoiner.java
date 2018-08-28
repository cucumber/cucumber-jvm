package cucumber.runtime.java;

public class StringJoiner {
    public static String join(String delimiter, Iterable<String> strings){
        //TODO: Java8 replace with StringJoiner.
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String string : strings) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(string);
        }
        return builder.toString();
    }
}
