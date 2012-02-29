package cucumber.formatter;

import cucumber.formatter.usage.AverageUsageStatisticStrategy;
import cucumber.formatter.usage.MedianUsageStatisticStrategy;
import cucumber.formatter.usage.UsageStatisticStrategy;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class FormatterFactoryTest {

    private final FormatterFactory formatterFactory = new FormatterFactory(Thread.currentThread().getContextClassLoader());

    @Test
    public void shouldInstantiateJsonFormatter() {
        assertThat(formatterFactory.createFormatter("json", System.out), is(JSONFormatter.class));
    }

    @Test
    public void shouldInstantiateJsonPrettyFormatter() {
        assertThat(formatterFactory.createFormatter("json-pretty", System.out), is(JSONPrettyFormatter.class));
    }

    @Test
    public void shouldInstantiatePrettyFormatter() {
        assertThat(formatterFactory.createFormatter("pretty", System.out), is(PrettyFormatter.class));
    }

    @Test
    public void shouldInstantiateProgressFormatter() {
        assertThat(formatterFactory.createFormatter("progress", System.out), is(ProgressFormatter.class));
    }

    @Test
    public void shouldInstantiateHtmlFormatter() {
        assertThat(formatterFactory.createFormatter("html", new File(System.getProperty("user.dir"))), is(HTMLFormatter.class));
    }


    @Test
    public void shouldInstantiateUsageFormatter() {
        Formatter formatter = formatterFactory.createFormatter("usage", mock(Appendable.class));
        
        assertThat(formatter, is(UsageFormatter.class));
        Map<String,UsageStatisticStrategy> statisticStrategies = ((UsageFormatter) formatter).statisticStrategies;
        assertEquals(statisticStrategies.size(), 2);
        assertThat(statisticStrategies.get("average"), is(AverageUsageStatisticStrategy.class));
        assertThat(statisticStrategies.get("median"), is(MedianUsageStatisticStrategy.class));
    }

    @Test
    public void shouldInstantiateCustomFormatterFromClassNameWithAppender() {
        StringWriter writer = new StringWriter();
        Formatter formatter = formatterFactory.createFormatter(TestFormatter.class.getName(), writer);
        assertThat(formatter, is(TestFormatter.class));
        assertSame(writer, ((TestFormatter) formatter).appendable);
    }

    @Test
    public void shouldInstantiateCustomFormatterFromClassNameWithDirFile() {
        File dir = new File(System.getProperty("user.dir"));
        Formatter formatter = formatterFactory.createFormatter(TestFormatter.class.getName(), dir);
        assertThat(formatter, is(TestFormatter.class));
        assertSame(dir, ((TestFormatter) formatter).dir);
    }
}
