package cucumber.runtime.java.surefire

import gherkin.formatter.PrettyFormatter
import gherkin.formatter.model.Result

import org.apache.maven.surefire.providerapi.AbstractProvider
import org.apache.maven.surefire.providerapi.ProviderParameters
import org.apache.maven.surefire.report.ReporterConfiguration
import org.apache.maven.surefire.report.ReporterException
import org.apache.maven.surefire.suite.RunResult
import org.apache.maven.surefire.testset.DirectoryScannerParameters
import org.apache.maven.surefire.testset.TestArtifactInfo
import org.apache.maven.surefire.testset.TestRequest
import org.apache.maven.surefire.testset.TestSetFailedException
import org.apache.maven.surefire.util.DirectoryScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import cucumber.resources.Consumer
import cucumber.resources.NoSuchResourceException
import cucumber.resources.Resource
import cucumber.resources.Resources
import cucumber.runtime.FeatureBuilder
import cucumber.runtime.Runtime
import cucumber.runtime.SnippetPrinter

class CucumberProvider extends AbstractProvider {
    Logger log = LoggerFactory.getLogger(CucumberProvider)

    Properties providerProperties
    TestArtifactInfo testArtifactInfo
    ReporterConfiguration reporterConfiguration
    ClassLoader testClassLoader
    DirectoryScannerParameters directoryScannerParameters
    DirectoryScanner directoryScanner
    TestRequest testRequest
    ProviderParameters providerParameters
    File basedir
    Runtime runtime

    CucumberProvider(ProviderParameters booterParameters) {
        this.providerParameters = booterParameters
        this.testClassLoader = booterParameters.getTestClassLoader()
        this.directoryScannerParameters = booterParameters.getDirectoryScannerParameters()
        this.providerProperties = booterParameters.getProviderProperties()
        this.testRequest = booterParameters.getTestRequest()
        this.basedir = directoryScannerParameters != null ? directoryScannerParameters.getTestClassesDirectory() : null
        this.testArtifactInfo = booterParameters.getTestArtifactInfo()
        this.reporterConfiguration = booterParameters.getReporterConfiguration()
        this.directoryScanner = booterParameters.getDirectoryScanner()
    }

    RunResult invoke(Object featureFile) throws TestSetFailedException, ReporterException {
        List features = []
        if (featureFile) {
            features = getFeatures(featureFile, null)
        } else {
            features = getFeatures('features', '.feature')
        }

        if (features) {
            if (!runtime) {
                initRuntime()
            }

            int totalScenarios = 0
            features.each {it.featureElements.each {totalScenarios += it.steps.size()}}

            def reporter = new SurefireReporter(System.out, false, true)
            features.each {feature ->
                log.info('Executing feature {}.', feature.featureUri)
                feature.run(runtime, reporter, reporter)
            }

            return new RunResult(reporter.completed, reporter.errors, reporter.failures, totalScenarios - reporter.completed)
        }

        return new RunResult(0, 0, 0, 0)
    }

    Iterator getSuites() {
        List featureFiles = []
        Resources.scan('features', '.feature', [
            consume: {Resource resource ->
                featureFiles.add(resource.path)
            }
        ] as Consumer)

        return featureFiles.iterator()
    }

    private getFeatures(String pathPrefix, String suffix) {
        List features = []
        FeatureBuilder builder = new FeatureBuilder(features)
        try {
            Resources.scan(pathPrefix, suffix, [
                consume: {Resource resource ->
                    builder.parse(resource, [])
                }
            ] as Consumer)
            log.info('Running Cucumber...')
            log.info('Found {} features.', features.size())
        } catch (NoSuchResourceException e) {
            //no features, do nothing
        }
        return features
    }

    private initRuntime() {
        runtime = new Runtime(['features'])

        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                new SnippetPrinter(System.out).printSnippets(runtime)
            }
        })
    }

    class SurefireReporter extends PrettyFormatter {
        int completed, errors, failures

        SurefireReporter(Appendable out, boolean monochrome, boolean executing) {
            super(out, monochrome, executing)
        }

        @Override
        void result(Result result) {
            super.result(result)
            completed++
            if (result.status == Result.FAILED) {
                failures++
            }
            if (result.error) {
                errors++
            }
        }
    }
}
