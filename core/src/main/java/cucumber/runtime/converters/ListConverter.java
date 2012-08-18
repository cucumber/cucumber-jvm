package cucumber.runtime.converters;

import cucumber.runtime.xstream.converters.SingleValueConverter;

import java.util.ArrayList;
import java.util.List;

public class ListConverter implements SingleValueConverter {
    private final String delimiter;
    private final SingleValueConverter delegate;

    public ListConverter(String delimiter, SingleValueConverter delegate) {
        this.delimiter = delimiter;
        this.delegate = delegate;
    }

    @Override
    public String toString(Object obj) {
        boolean first = true;
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (Object elem : (List) obj) {
                if (!first) {
                    sb.append(delimiter);
                }
                sb.append(delegate.toString(elem));
                first = false;
            }
            return sb.toString();
        } else {
            return delegate.toString(obj);
        }
    }

    @Override
    public Object fromString(String s) {
        final String[] strings = s.split(delimiter);
        List<Object> list = new ArrayList<Object>(strings.length);
        for (String elem : strings) {
            list.add(delegate.fromString(elem));
        }
        return list;
    }

    @Override
    public boolean canConvert(Class type) {
        return List.class.isAssignableFrom(type);
    }
}
