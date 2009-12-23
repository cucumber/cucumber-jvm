package cuke4duke.mojo;

import cuke4duke.ant.GemTask;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base for all JRuby mojos.
 *
 * @requiresDependencyResolution test
 */
public abstract class AbstractJRubyMojo extends AbstractMojo {

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
     * @parameter expression="${cucumber.gemDirectory}"
     * @required
     */
    protected File gemDirectory;

    /**
     * The project compile classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    protected List<String> compileClasspathElements;

    /**
     * The plugin dependencies.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List<Artifact> pluginArtifacts;

    /**
     * The project test classpath
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    protected List<String> testClasspathElements;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    protected abstract List<String> getJvmArgs();

    /**
     * Installs a gem. Sources used:
     * <ul>
     * <li>http://gems.rubyforge.org</li>
     * <li>http://gemcutter.org/</li>
     * <li>http://gems.github.com</li>
     * </ul>
     *
     * @param gemArgs name and optional arguments. Example:
     *                <ul>
     *                <li>awesome</li>
     *                <li>awesome --version 9.8</li>
     *                <li>awesome --version 9.8 --source http://some.gem.server</li>
     *                </ul>
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          if gem installation fails.
     */
    protected void installGem(String gemArgs) throws MojoExecutionException {
        GemTask gem = new GemTask();
        if (gemDirectory != null && gemDirectory.exists()) {
            gem.setDir(gemDirectory);
        }
        gem.setProject(getProject());
        gem.setArgs(gemArgs);
        gem.execute();
    }

    protected File jrubyHome() {
        return new File(localRepository.getBasedir(), ".jruby");
    }

    protected Project getProject() throws MojoExecutionException {
        Project project = new Project();
        project.setBaseDir(mavenProject.getBasedir());
        project.setProperty("jruby.home", jrubyHome().getAbsolutePath());
        project.addBuildListener(new LogAdapter());

        Path jrubyClasspath = new Path(project);
        project.addReference("jruby.classpath", jrubyClasspath);

        try {
            append(jrubyClasspath, compileClasspathElements);
            append(jrubyClasspath, pluginArtifacts);
            append(jrubyClasspath, testClasspathElements);
            return project;
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("error resolving dependencies", e);
        }
    }

    protected void append(Path classPath, List<?> artifacts) throws DependencyResolutionRequiredException {
        List<String> list = new ArrayList<String>(artifacts.size());

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

        Path p = new Path(classPath.getProject());
        p.setPath(StringUtils.join(list.iterator(), File.pathSeparator));
        classPath.append(p);
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
            String message = event.getMessage();
            switch (priority) {
                case Project.MSG_ERR:
                    log.error(message);
                    break;

                case Project.MSG_WARN:
                    log.warn(message);
                    break;

                case Project.MSG_INFO:
                    log.info(message);
                    break;

                case Project.MSG_VERBOSE:
                    log.debug(message);
                    break;

                case Project.MSG_DEBUG:
                    log.debug(message);
                    break;

                default:
                    log.info(message);
                    break;
            }
        }
    }
}
