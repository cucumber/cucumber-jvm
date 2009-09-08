package cuke4duke.mojo;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CucumberMojoTest {

    private CucumberMojo mojo;

    @Before
    public void setUp() {
        this.mojo = new CucumberMojo();
    }

    @Test
    public void shouldAllowZeroCucumberArgs() {
        mojo.cucumberArgs = null;
        mojo.addCucumberArgs();
    }

    @Test
    public void shouldAddCucumberArgs() {
        String cucumberArg = "testArg";
        mojo.cucumberArgs = new ArrayList<String>();
        mojo.cucumberArgs.add(cucumberArg);
        assertTrue(mojo.addCucumberArgs().contains(cucumberArg));
    }

    @Test
    public void shouldAllowZeroAddCucumberArgs() {
        mojo.extraCucumberArgs = null;
        mojo.addCucumberArgs();
    }

    @Test
    public void shouldSplitAddCucumberArgsIntoRealCucumberArgs() {
        mojo.extraCucumberArgs = "arg1 arg2 arg3";
        List<String> referenceList = new ArrayList<String>();
        referenceList.add("arg1");
        referenceList.add("arg2");
        referenceList.add("arg3");
        assertEquals(referenceList, mojo.addCucumberArgs());
    }

    @Test
    public void shouldHandleOneAddCucumberArg() {
        mojo.extraCucumberArgs = "arg1";
        List<String> referenceList = new ArrayList<String>();
        referenceList.add("arg1");
        assertEquals(referenceList, mojo.addCucumberArgs());
    }
}
