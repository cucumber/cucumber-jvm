package cuke4duke.mojo;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
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
    
    @Test
    public void shouldHandleNullJvmArg() throws MojoExecutionException {
        setUpMojo();
        mojo.jvmArgs = Arrays.asList(new String[]{"arg1", null});
        List<String> referenceList = new ArrayList<String>();
        referenceList.add("arg1");
        Assert.assertNotNull(mojo.jruby(new ArrayList<String>()));
    }
    
    @Test
    public void shouldHandleEmptyJvmArg() throws MojoExecutionException {
        setUpMojo();
        mojo.jvmArgs = Arrays.asList(new String[]{"arg1", ""});
        List<String> referenceList = new ArrayList<String>();
        referenceList.add("arg1");
        Assert.assertNotNull(mojo.jruby(new ArrayList<String>()));
    }

    private void setUpMojo() {
        mojo.launchDirectory = new File(".");
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setFile(new File("../."));
        mojo.compileClasspathElements = new ArrayList<String>();
        mojo.pluginArtifacts = new ArrayList<Artifact>();
        mojo.testClasspathElements = new ArrayList<String>();
        mojo.localRepository = new DefaultArtifactRepository("", "", new DefaultRepositoryLayout());
    }
}
