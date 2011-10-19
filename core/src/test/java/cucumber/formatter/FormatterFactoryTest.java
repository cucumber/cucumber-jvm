package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

public class FormatterFactoryTest {

    private final FormatterFactory formatterFactory = new FormatterFactory();

    @Test
    public void shouldInstantiateJsonFormatter() {
        assertThat(formatterFactory.createFormatter("json", System.out), is(JSONFormatter.class));
    }

    @Test
    public void shouldInstantiatePrettyFormatter() {
        assertThat(formatterFactory.createFormatter("pretty", System.out), is(PrettyFormatter.class));
    }

    @Test
    public void shouldInstantiateHtmlFormatter() {
        assertThat(formatterFactory.createFormatter("html", System.out), is(HTMLFormatter.class));
    }

    @Test
    public void shouldInstantiateCustomFormatterFromClassName() {
        assertThat(formatterFactory.createFormatter(TestFormatter.class.getName(), System.out), is(TestFormatter.class));
    }

    @Test
    public void shouldInstantiateCustomFormatterFromClassNameWithAppender() {
        StringWriter writer = new StringWriter();
        Formatter formatter = formatterFactory.createFormatter(TestFormatterWithAppendable.class.getName(), writer);
        assertThat(formatter, is(TestFormatterWithAppendable.class));
        assertSame(writer, ((TestFormatterWithAppendable) formatter).appendable);
    }

}
