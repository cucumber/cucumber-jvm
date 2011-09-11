package cucumber.runtime;

import static cucumber.runtime.ParameterPatternExchanger.ExchangeMatchsWithPattern;
import static cucumber.runtime.ParameterPatternExchanger.ExchangeMatchesWithReplacement;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.junit.Test;

public class ParameterPatternExchanger_Test {
    private static final String ASingleSpace = " ";
    private Class<?> anyType = Integer.TYPE;
	private Pattern singleDigit = Pattern.compile("(\\d)");
	private ParameterPatternExchanger exchanger = ExchangeMatchsWithPattern(singleDigit, anyType);

	@Test
	public void replacesMatchWithPattern() {
		assertThat(afterExchangeOn("1"), theNameIs("(\\d)"));
	}

	@Test
	public void replacesMultipleMatchesWithPattern() {
		assertThat(afterExchangeOn("13"), theNameIs("(\\d)(\\d)"));
	}
	
	@Test
    public void replaceMatchWithSpace() throws Exception {
		assertThat(exchanger.replaceMatchWithSpace("4"), is(ASingleSpace));
    }
	
	@Test
    public void replacesMatchesWithReplacement() throws Exception {
        exchanger = ExchangeMatchesWithReplacement(singleDigit, "the given replacement", anyType);
        assertThat(afterExchangeOn("1"), theNameIs("the given replacement"));
    }

	private String afterExchangeOn(String name) {
		return exchanger.exchangeMatches(name);
	}
	
	private Matcher<String> theNameIs(String value) {
	    return is(value);
    }
}