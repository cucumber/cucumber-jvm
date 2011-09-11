package cucumber.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.junit.Test;

public class ParameterPatternExchanger_Test {
	private Pattern singleDigit = Pattern.compile("(\\d)");
	private ParameterPatternExchanger exchanger = new ParameterPatternExchanger(singleDigit);

	@Test
	public void replacesMatchWithPattern() {
		assertThat(afterExhangeOn("1"), theNameIs("(\\d)"));
	}

	@Test
	public void replacesMultipleMatchesWithPattern() {
		assertThat(afterExhangeOn("13"), theNameIs("(\\d)(\\d)"));
	}
	
	@Test
    public void replaceMatchWithSpace() throws Exception {
		assertThat(exchanger.replaceMatchWithSpace("4"), is(" "));
    }

	private String afterExhangeOn(String name) {
		return exchanger.replaceMatches(name);
	}
	
	private Matcher<String> theNameIs(String value) {
	    return is(value);
    }
}