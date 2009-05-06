package cucumber.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base for all JRuby mojos.
 *
 * @requiresDependencyResolution compile
 */
public abstract class AbstractJRubyMojo extends AbstractMojo {

    protected boolean shouldFork = true;

    /**
     * @parameter expression="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * @parameter expression="${project.basedir}"
     * @required
     */
    protected File launchDirectory;

    /**
     * @parameter expression="${jruby.home}"
     */
    protected String jrubyHome;

    /**
     * The amount of memory to use when forking JRuby. Default is "384m".
     *
     * @parameter expression="${jruby.launch.memory}"
     */
    protected String jrubyLaunchMemory = "384m";

    /**
     * The project compile classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List compileClasspathElements;

    /**
     * The plugin dependencies.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List pluginArtifacts;

    protected Java jruby(List<String> args) throws MojoExecutionException {
        launchDirectory.mkdirs();
        Project project = null;
        try {
            project = getProject();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("error resolving dependencies", e);
        }

        Java java = new Java();
        java.setProject(project);
        java.setClassname("org.jruby.Main");
        java.setFailonerror(true);

        Commandline.Argument arg;

        if (shouldFork) {
            java.setFork(true);
            java.setDir(launchDirectory);

            arg = java.createJvmarg();
            arg.setValue("-Xmx" + jrubyLaunchMemory);
            Environment.Variable classpath = new Environment.Variable();

            Path p = new Path(java.getProject());
            p.add((Path) project.getReference("maven.plugin.classpath"));
            p.add((Path) project.getReference("maven.compile.classpath"));
            p.add((Path) project.getReference("maven.test.classpath"));
            classpath.setKey("JRUBY_PARENT_CLASSPATH");
            classpath.setValue(p.toString());

            java.addEnv(classpath);
        }

        if (jrubyHome != null) {
            Environment.Variable v = new Environment.Variable();
            v.setKey("jruby.home");
            v.setValue(jrubyHome);
            java.addSysproperty(v);
        }

        Path p = java.createClasspath();
        p.add((Path) project.getReference("maven.plugin.classpath"));
        p.add((Path) project.getReference("maven.compile.classpath"));
        getLog().debug("java classpath: " + p.toString());

        for (String s : args) {
            arg = java.createArg();
            arg.setValue(s);
        }

        return java;
    }

    @SuppressWarnings({"unchecked"})
    protected void installGem(List<String> gem) throws MojoExecutionException {
        List args = new ArrayList();
        args.add("-S");
        // maybe_install_gems only takes a list of gems, no versions or sources
        if (gem.size() > 1) {
            args.add("gem");
            args.add("install");
        } else {
            args.add("maybe_install_gems");
        }
        args.addAll(gem);

        Java jruby = jruby(args);
        jruby.execute();
    }

    protected Project getProject() throws DependencyResolutionRequiredException {
        Project project = new Project();
        project.setBaseDir(mavenProject.getBasedir());
        project.addBuildListener(new LogAdapter());
        addReference(project, "maven.compile.classpath", compileClasspathElements);
        addReference(project, "maven.plugin.classpath", pluginArtifacts);
        return project;
    }

    @SuppressWarnings({"unchecked"})
    protected void addReference(Project project, String reference, List artifacts)
            throws DependencyResolutionRequiredException {
        List list = new ArrayList(artifacts.size());

        for (Object elem : artifacts) {
            String path;
            if (elem instanceof Artifact) {
                Artifact a = (Artifact) elem;
                File file = a.getFile();
                if (file == null) {
                    throw new DependencyResolutionRequiredException(a);
                }
                path = file.getPath();
            } else {
                path = elem.toString();
            }
            list.add(path);
        }

        Path p = new Path(project);
        p.setPath(StringUtils.join(list.iterator(), File.pathSeparator));
        project.addReference(reference, p);
    }

    public static <T> List<T> listify(T... objects) {
        List<T> res = new ArrayList<T>();
        res.addAll(Arrays.asList(objects));
        return res;
    }

    public class LogAdapter implements BuildListener {
        public void buildStarted(BuildEvent event) {
            log(event);
        }

        public void buildFinished(BuildEvent event) {
            log(event);
        }

        public void targetStarted(BuildEvent event) {
            log(event);
        }

        public void targetFinished(BuildEvent event) {
            log(event);
        }

        public void taskStarted(BuildEvent event) {
            log(event);
        }

        public void taskFinished(BuildEvent event) {
            log(event);
        }

        public void messageLogged(BuildEvent event) {
            log(event);
        }

        private void log(BuildEvent event) {
            int priority = event.getPriority();
            Log log = getLog();
            switch (priority) {
                case Project.MSG_ERR:
                    log.error(event.getMessage());
                    break;

                case Project.MSG_WARN:
                    log.warn(event.getMessage());
                    break;

                case Project.MSG_INFO:
                    log.info(event.getMessage());
                    break;

                case Project.MSG_VERBOSE:
                    log.debug(event.getMessage());
                    break;

                case Project.MSG_DEBUG:
                    log.debug(event.getMessage());
                    break;

                default:
                    log.info(event.getMessage());
                    break;
            }
        }
    }
}
