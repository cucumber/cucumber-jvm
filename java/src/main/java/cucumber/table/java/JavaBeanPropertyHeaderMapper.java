package cucumber.table.java;

import org.apache.commons.lang3.StringUtils;

import cucumber.table.TableHeaderMapper;

public class JavaBeanPropertyHeaderMapper implements TableHeaderMapper {

    @Override
    public String map(String originalHeaderName) {
        String[] splitted = StringUtils.split(StringUtils.normalizeSpace(originalHeaderName));
        splitted[0] = StringUtils.uncapitalize(splitted[0]);
        for (int i = 1; i < splitted.length; i++) {
            splitted[i] = StringUtils.capitalize(splitted[i]);
        }
        return StringUtils.join(splitted);
    }

}
