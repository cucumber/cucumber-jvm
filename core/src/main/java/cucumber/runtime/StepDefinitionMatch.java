package cucumber.runtime;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.converters.EnumConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.converters.TimeConverter;
import cucumber.table.DataTable;
import cucumber.table.TableConverter;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;
import gherkin.util.Mapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static gherkin.util.FixJava.map;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final transient String uri;
    // The official JSON gherkin format doesn't have a step attribute, so we're marking this as transient
    // to prevent it from ending up in the JSON.
    private final transient Step step;
    private final LocalizedXStreams localizedXStreams;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, String uri, Step step, LocalizedXStreams localizedXStreams) {
        super(arguments, stepDefinition.getLocation(false));
        this.stepDefinition = stepDefinition;
        this.uri = uri;
        this.step = step;
        this.localizedXStreams = localizedXStreams;
    }

    public void runStep(I18n i18n) throws Throwable {
        try {
            Object[] args = transformedArgs(stepDefinition.getParameterTypes(), step, localizedXStreams.get(i18n), i18n.getLocale());
            stepDefinition.execute(i18n, args);
        } catch (CucumberException e) {
            throw e;
        } catch (Throwable t) {
            throw removeFrameworkFramesAndAppendStepLocation(t, getStepLocation());
        }
    }

    /**
     * @param parameterTypes types of the stepdefs args. Some backends will pass null if they can't determine types or arity.
     * @param step           the step to run
     * @param xStream        used to convert a string to declared stepdef arguments
     * @param locale         the feature's locale
     * @return an Array matching the types or {@code parameterTypes}, or an array of String if {@code parameterTypes} is null
     */
    private Object[] transformedArgs(List<ParameterType> parameterTypes, Step step, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        if (xStream == null) {
            throw new NullPointerException("xStream");
        }
        int argumentCount = getArguments().size();
        if (step.getDocString() != null) argumentCount++;
        if (step.getRows() != null) argumentCount++;
        if (parameterTypes != null) {
            if (parameterTypes.size() != argumentCount) {
                throw arityMismatch(parameterTypes, step);
            }
        } else {
            // Some backends, like ClojureBackend, don't know the arity and therefore pass in null.
            parameterTypes = Utils.listOf(argumentCount, new ParameterType(String.class, null));
        }

        Object[] result = new Object[argumentCount];
        ConverterLookup converterLookup = xStream.getConverterLookup();

        int n = 0;
        for (Argument a : getArguments()) {
            SingleValueConverter singleValueConverter;
            TimeConverter timeConverter = null;
            ParameterType parameterType = parameterTypes.get(n);
            if(parameterType.getSingleValueConverter() != null) {
                singleValueConverter = parameterType.getSingleValueConverter();
            } else if (parameterType.getDateFormat() != null) {
                timeConverter = TimeConverter.getInstance(parameterType, locale);
                timeConverter.setOnlyFormat(parameterType.getDateFormat(), locale);
                singleValueConverter = timeConverter;
            } else if (parameterType.getParameterClass().isEnum()) {
                singleValueConverter = new EnumConverter(locale, (Class<? extends Enum>) parameterType.getParameterClass());
            } else {
                Converter converter = converterLookup.lookupConverterForType(parameterType.getParameterClass());
                if(converter instanceof SingleValueConverter) {
                    singleValueConverter = (SingleValueConverter) converter;
                } else {
                    throw new CucumberException(String.format(
                            "Don't know how to convert %s into %s.\n" +
                                    "Try writing your own converter:\n" +
                                    "\n" +
                                    "@%s(%sConverter.class)\n" +
                                    "public class %s {}\n",
                            a.getVal(),
                            parameterType.getParameterClass().getName(),
                            XStreamConverter.class.getName(),
                            parameterType.getParameterClass().getSimpleName(),
                            parameterType.getParameterClass().getSimpleName()
                    ));
                }
            }
            try {
                result[n] = singleValueConverter.fromString(a.getVal());
            } finally {
                if (timeConverter != null) {
                    timeConverter.removeOnlyFormat();
                }
            }
            n++;
        }

        if (step.getRows() != null) {
            ParameterType parameterType = parameterTypes.get(n);
            xStream.setDateFormat(parameterType.getDateFormat());
            try {
                result[n] = tableArgument(step, n, xStream, parameterType.getDateFormat());
            } finally {
                xStream.unsetDateFormat();
            }
        } else if (step.getDocString() != null) {
            result[n] = step.getDocString().getValue();
        }
        return result;
    }

    private CucumberException arityMismatch(List<ParameterType> parameterTypes, Step step) {
        List<Argument> arguments = createArgumentsForErrorMessage(step);
        return new CucumberException(String.format(
                "Arity mismatch: Step Definition '%s' with pattern /%s/ is declared with %s parameters%s. However, the gherkin step matched %s arguments%s. \nStep: %s%s",
                stepDefinition.getLocation(true),
                stepDefinition.getPattern(),
                parameterTypes.size(),
                parameterTypes.isEmpty() ? "" : " " + parameterTypes.toString(),
                arguments.size(),
                arguments.isEmpty() ? "" : " " + arguments.toString(),
                step.getKeyword(),
                step.getName()
        ));
    }

    private List<Argument> createArgumentsForErrorMessage(Step step) {
        List<Argument> arguments = new ArrayList<Argument>(getArguments());
        if (step.getDocString() != null) {
            arguments.add(new Argument(-1, "DocString:" + step.getDocString().getValue()));
        }
        if (step.getRows() != null) {
            List<List<String>> rows = map(step.getRows(), new Mapper<DataTableRow, List<String>>() {
                @Override
                public List<String> map(DataTableRow row) {
                    return row.getCells();
                }
            });
            arguments.add(new Argument(-1, "Table:" + rows.toString()));
        }
        return arguments;
    }

    private Object tableArgument(Step step, int argIndex, XStream xStream, String dateFormat) {
        DataTable table = new DataTable(step.getRows(), new TableConverter(xStream));

        Type listType = getGenericListType(argIndex);
        if (listType != null) {
            return table.asList(listType);
        } else {
            return table;
        }
    }

    private Type getGenericListType(int argIndex) {
        Type result = null;
        List<ParameterType> parameterTypes = stepDefinition.getParameterTypes();
        if (parameterTypes != null) {
            ParameterType parameterType = parameterTypes.get(argIndex);
            Type[] actualTypeArguments = parameterType.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                result = actualTypeArguments[0];
            }
        }
        return result;
    }

    public Throwable removeFrameworkFramesAndAppendStepLocation(Throwable error, StackTraceElement stepLocation) {
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        if (stackTraceElements.length == 0 || stepLocation == null) {
            return error;
        }

        int newStackTraceLength;
        for (newStackTraceLength = 1; newStackTraceLength < stackTraceElements.length; ++newStackTraceLength) {
            if (stepDefinition.isDefinedAt(stackTraceElements[newStackTraceLength - 1])) {
                break;
            }
        }
        StackTraceElement[] newStackTrace = new StackTraceElement[newStackTraceLength + 1];
        System.arraycopy(stackTraceElements, 0, newStackTrace, 0, newStackTraceLength);
        newStackTrace[newStackTraceLength] = stepLocation;
        error.setStackTrace(newStackTrace);
        return error;
    }

    public String getPattern() {
        return stepDefinition.getPattern();
    }

    public StackTraceElement getStepLocation() {
        return step.getStackTraceElement(uri);
    }

    public String getStepName() {
        return step.getName();
    }
}
