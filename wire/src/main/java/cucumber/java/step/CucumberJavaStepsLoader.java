package cucumber.java.step;

import cucumber.api.StepDefinitionReporter;
import cucumber.java.CucumberOptions;
import cucumber.java.hook.HookRegistrar;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * This class is responsible for loading all of the steps that are implemented by classes that are on the
 * classpath. Classes that are on the classpath contained within jar files cannot be loaded currently. Only
 * the classes in directories that are on the classpath can be loaded. This is a limitation of the MultiLoader
 * class in use.
 *
 * The RuntimeGlue class is then used to register all of the before and after hooks with the HookRegistrar.
 * It also registers all of the steps with the StepManager. Part of registering the steps is to also implement
 * the method which invokes steps.
 *
 * Gaps in functionality or enhancements are identified below as TODOs.
 */
public class CucumberJavaStepsLoader implements StepsLoader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalizedXStreams localizedXStreams = new LocalizedXStreams(CucumberJavaStepsLoader.class.getClassLoader());
    private I18n i18n;

    public CucumberJavaStepsLoader() {
        // TODO: Language can be specified in different ways...?
        i18n = new I18n(Locale.getDefault().getLanguage());
    }

    public void loadSteps() {
        ClassLoader classLoader = CucumberJavaStepsLoader.class.getClassLoader();
        ResourceLoader loader = new MultiLoader(classLoader);
        JavaBackend backend = new JavaBackend(loader);
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        RuntimeGlue glue = new RuntimeGlue(undefinedStepsTracker, localizedXStreams);

        // Load all steps on the classpath
        // TODO: Provide another command-line option to specify which packages should be searched?
        backend.loadGlue(glue, Arrays.asList( new String[] { "" } ));

        registerBeforeHooks(glue);
        registerAfterHooks(glue);
        registerSteps(glue);
    }

    private void registerBeforeHooks(RuntimeGlue glue) {
        List<HookDefinition> beforeHooks = glue.getBeforeHooks();
        if (beforeHooks != null) {
            for (HookDefinition beforeHook : beforeHooks) {
                HookRegistrar.registerBeforeHook(beforeHook);
            }
        }
    }

    private void registerAfterHooks(RuntimeGlue glue) {
        List<HookDefinition> afterHooks = glue.getAfterHooks();
        if (afterHooks != null) {
            for (HookDefinition afterHook : afterHooks) {
                HookRegistrar.registerAfterHook(afterHook);
            }
        }
    }

    private void registerSteps(RuntimeGlue glue) {
        glue.reportStepDefinitions(
                new StepDefinitionReporter() {
                    public void stepDefinition(final StepDefinition stepDefinition) {
                        if (CucumberOptions.getOptions().isVerbose()) {
                            logger.debug(
                                    "Found step: pattern=\"" + stepDefinition.getPattern() + "\" " +
                                            "location=" + stepDefinition.getLocation(true)
                            );
                        }

                        // TODO: Support Java8 lambdas
                        // Cucumber-Wire expects the source in a specific format. Anything else will cause it to crash.
                        // This has not been tested with Java8 lambdas yet but I suspect it will fail since the getLocation()
                        // method below returns a different format than is expected here.
                        StepInfo stepInfo = new StepInfo(
                                stepDefinition.getPattern(),
                                stepDefinition.getLocation(false) + ":0" // TODO: Get the line number from ???
                        ) {
                            public InvokeResult invokeStep(InvokeArgs args) {
                                return CucumberJavaStepsLoader.this.invokeStep(stepDefinition, args);
                            }
                        };

                        StepManager.addStep(stepInfo);
                    }
                }
        );
    }

    /**
     * This method takes advantage of the existing StepDefinitionMatch class to invoke a step. The benefit here is
     * that class manages converting the input arguments to the arguments expected by the step method.
     *
     * @param stepDefinition the definition of the step to be invoked
     * @param args the step arguments provided by cucumber via the invoke command
     * @return the invoke result, whether success or failure
     */
    private InvokeResult invokeStep(StepDefinition stepDefinition, InvokeArgs args) {
        // The following are for constructing the feature step (below)
        List<Comment> comments = new ArrayList<Comment>();
        String stepKeyword = ""; // TODO: The wire protocol doesn't pass this to us
        String stepName = stepDefinition.getPattern();
        Integer line = 0; // TODO: The wire protocol doesn't pass this to us
        List<DataTableRow> rows = convertToDataTableRows(args);
        DocString docString = null; // TODO: No idea what this is

        // The following are for constructing the step definition match
        List<Argument> arguments = convertToArguments(args);
        String featurePath = ""; // TODO: The wire protocol doesn't pass this to us
        Step featureStep = new Step(comments, stepKeyword, stepName, line, rows, docString);

        StepDefinitionMatch match = new StepDefinitionMatch(arguments, stepDefinition, featurePath, featureStep, localizedXStreams);

        try {
            match.runStep(i18n);
            return InvokeResult.success();
        } catch (Throwable throwable) {
            // Is there a better message?
            return InvokeResult.failure(throwable.getMessage());
        }
    }

    private List<DataTableRow> convertToDataTableRows(InvokeArgs args) {
        List<DataTableRow> rows = null;

        if (args.getTableArg() != null) {
            List<String> inputCols = args.getTableArg().getColumns();
            if (inputCols != null && inputCols.size() > 0) {
                List<Comment> comments = new ArrayList<Comment>();
                Integer line = 0; // TODO: The wire protocol doesn't pass this to us
                DataTableRow row = new DataTableRow(comments, inputCols, line);
                if (rows == null) {
                    rows = new ArrayList<DataTableRow>();
                }
                rows.add(row);
            }

            List<List<String>> inputRows = args.getTableArg().getRows();
            if (inputRows != null) {
                for (List<String> inputRow : inputRows) {
                    List<Comment> comments = new ArrayList<Comment>();
                    Integer line = 0; // TODO: The wire protocol doesn't pass this to us
                    DataTableRow row = new DataTableRow(comments, inputRow, line);
                    if (rows == null) {
                        rows = new ArrayList<DataTableRow>();
                    }
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    private List<Argument> convertToArguments(InvokeArgs args) {
        List<Argument> arguments = new ArrayList<Argument>();

        if (args.getInvokeArgs() != null) {
            for (String inputArg : args.getInvokeArgs()) {
                Integer offset = null; // TODO: The invoke command doesn't pass this to us
                // TODO: Lookup the offset from ???
                Argument arg = new Argument(offset, inputArg);
                arguments.add(arg);
            }
        }

        return arguments;
    }

}
