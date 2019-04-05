package cucumber.runtime.junit;

import cucumber.runner.Runner;
import cucumber.runner.RunnerSupplier;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;

import java.io.Serializable;
import java.lang.reflect.Method;


class PickleRunners {

    static abstract class PickleRunner extends FrameworkMethod {
    	
    	static Method method;
    	
    	static Method method() {
    		if (method != null) return method;
            try {
                method = Object.class.getMethod("toString");
                return method;
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException(e);
            }
    	}
    	
    	PickleRunner() {
    		super(method());
		}
    	
        abstract void run(RunNotifier notifier);

        RunNotifier notifier;
        
        void setNotifier(RunNotifier notifier) {
        	this.notifier = notifier;
        }
        
        void run() {
        	run(notifier);
        }
        
        abstract Description getDescription();

        abstract Description describeChild(PickleStep step);

    }

    static PickleRunner withNoStepDescriptions(String featureName, RunnerSupplier runnerSupplier, PickleEvent pickleEvent, JUnitOptions jUnitOptions) {
        return new NoStepDescriptions(featureName, runnerSupplier, pickleEvent, jUnitOptions);
    }


    static final class NoStepDescriptions extends PickleRunner {
        private final String featureName;
        private final RunnerSupplier runnerSupplier;
        private final PickleEvent pickleEvent;
        private final JUnitOptions jUnitOptions;
        private Description description;

        NoStepDescriptions(String featureName, RunnerSupplier runnerSupplier, PickleEvent pickleEvent, JUnitOptions jUnitOptions) {
            this.featureName = featureName;
            this.runnerSupplier = runnerSupplier;
            this.pickleEvent = pickleEvent;
            this.jUnitOptions = jUnitOptions;
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                String className = createName(featureName, jUnitOptions.filenameCompatibleNames());
                String name = getPickleName(pickleEvent, jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleId(pickleEvent));
            }
            return description;
        }

        @Override
        public Description describeChild(PickleStep step) {
            throw new UnsupportedOperationException("This pickle runner does not wish to describe its children");
        }

        @Override
        public void run(final RunNotifier notifier) {
            // Possibly invoked by a thread other then the creating thread
            Runner runner = runnerSupplier.get();
            JUnitReporter jUnitReporter = new JUnitReporter(runner.getBus(), jUnitOptions);
            jUnitReporter.startExecutionUnit(this, notifier);
            runner.runPickle(pickleEvent);
            jUnitReporter.finishExecutionUnit();
        }
    }

    private static String getPickleName(PickleEvent pickleEvent, boolean useFilenameCompatibleNames) {
        final String name = pickleEvent.pickle.getName();
        return createName(name, useFilenameCompatibleNames);
    }


    private static String createName(final String name, boolean useFilenameCompatibleNames) {
        if (name.isEmpty()) {
            return "EMPTY_NAME";
        }

        if (useFilenameCompatibleNames) {
            return makeNameFilenameCompatible(name);
        }

        return name;
    }

    private static String makeNameFilenameCompatible(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    static final class PickleId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;
        private int pickleLine;

        PickleId(String uri, int pickleLine) {
            this.uri = uri;
            this.pickleLine = pickleLine;
        }

        PickleId(PickleEvent pickleEvent) {
            this(pickleEvent.uri, pickleEvent.pickle.getLocations().get(0).getLine());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PickleId that = (PickleId) o;
            return pickleLine == that.pickleLine && uri.equals(that.uri);
        }

        @Override
        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + pickleLine;
            return result;
        }

        @Override
        public String toString() {
            return uri + ":" + pickleLine;
        }
    }

}
