package cucumber.runtime.table;

import cucumber.api.TableConverter;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.xstream.LocalizedXStreams;

import java.util.Locale;

@Deprecated
public class XStreamToDataTableTest extends ToTableContract {
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private static final ParameterInfo PARAMETER_INFO = new ParameterInfo(null, XStreamToDataTableTest.DD_MM_YYYY);
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Override
    protected TableConverter createTableConverter() {
        return new XStreamTableConverter(new LocalizedXStreams(classLoader).get(Locale.US), PARAMETER_INFO);
    }
}
