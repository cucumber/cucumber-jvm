package cucumber.runtime;

import cuke4duke.internal.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import static org.junit.Assert.*;

public class UtilsTest {
    @Test
    public void shouldCreateEnglishLocale() {
        Assert.assertEquals(Locale.ENGLISH, Utils.localeFor("en"));
    }

    @Test
    public void shouldCreateUSLocale() {
        assertEquals(Locale.US, Utils.localeFor("en-US"));
    }

    @Test
    public void shouldFormatLolcatDoubles() throws ParseException {
        assertEquals(10.4, NumberFormat.getInstance(Utils.localeFor("en-LOL")).parse("10.4").doubleValue(), 0.0);
    }

    @Test
    public void shouldFormatEnglishDoubles() throws ParseException {
        assertEquals(10.4, NumberFormat.getInstance(Utils.localeFor("en-US")).parse("10.4").doubleValue(), 0.0);
    }

    @Test
    public void shouldFormatNorwegianDoubles() throws ParseException {
        assertEquals(10.4, NumberFormat.getInstance(Utils.localeFor("no")).parse("10,4").doubleValue(), 0.0);
    }

    @Test
    public void shouldFormatNorwegianDoublesWithEnglishLocaleDifferently() throws ParseException {
        assertEquals(104.0, NumberFormat.getInstance(Utils.localeFor("en-US")).parse("10,4").doubleValue(), 0.0);
    }
}
