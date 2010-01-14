package cuke4duke.webdriver;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

// See http://wiki.github.com/aslakhellesoy/cuke4duke/debug-cuke4duke-steps
public class Debugging {
    @Test
    public void letsDebug() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        WebDriverFacade wdf = new WebDriverFacade();
        ResultsPage rp = new ResultsPage(wdf);
        SearchPage sp = new SearchPage(wdf);

        sp.visit();
        sp.search("cucumber github");
        rp.shouldSee("<em>Cucumber</em> itself is written in Ruby");
    }
}
