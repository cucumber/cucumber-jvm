package cucumber.table.java;

import java.util.regex.Pattern;

import cucumber.table.TableHeaderMapper;

public class JavaBeanPropertyHeaderMapper implements TableHeaderMapper {
    
    private static final String WHITESPACE = " ";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public String map(String originalHeaderName) {
        String[] splitted = normalizeSpace(originalHeaderName).split(WHITESPACE);
        splitted[0] = uncapitalize(splitted[0]);
        for (int i = 1; i < splitted.length; i++) {
            splitted[i] = capitalize(splitted[i]);
        }
        return join(splitted);
    }

    private String join(String[] splitted) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<splitted.length;i++) {
            sb.append(splitted[i]);
        }
        return sb.toString();
    }

    private String normalizeSpace(String originalHeaderName) {
        return WHITESPACE_PATTERN.matcher(originalHeaderName.trim()).replaceAll(WHITESPACE);
    }

    private String capitalize(String string) {
        return new StringBuilder(string.length()).append(Character.toTitleCase(string.charAt(0))).append(string.substring(1)).toString();
    }
    
    private String uncapitalize(String string) {
        return new StringBuilder(string.length()).append(Character.toLowerCase(string.charAt(0))).append(string.substring(1)).toString();
    }

}
