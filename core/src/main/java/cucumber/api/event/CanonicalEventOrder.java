package cucumber.api.event;

import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

final class CanonicalEventOrder implements Comparator<Event> {

    private static final FixedEventOrderComparator fixedOrder = new FixedEventOrderComparator();
    private static final TestCaseEventComparator testCaseOrder = new TestCaseEventComparator();

    @Override
    public int compare(Event a, Event b) {
        int fixedOrder = CanonicalEventOrder.fixedOrder.compare(a, b);
        if (fixedOrder != 0) {
            return fixedOrder;
        }

        if (!(a instanceof TestCaseEvent && b instanceof TestCaseEvent)) {
            return fixedOrder;
        }

        return testCaseOrder.compare((TestCaseEvent) a, (TestCaseEvent) b);
    }

    private static final class FixedEventOrderComparator implements Comparator<Event> {

        private final List<Class<? extends Event>> fixedOrder = asList(
            (Class<? extends Event>)
                TestRunStarted.class,
            TestSourceRead.class,
            SnippetsSuggestedEvent.class,
            StepDefinedEvent.class,
            TestCaseEvent.class,
            TestRunFinished.class
        );

        @Override
        public int compare(final Event a, final Event b) {
            return Integer.compare(requireInFixOrder(a.getClass()), requireInFixOrder(b.getClass()));
        }

        private int requireInFixOrder(Class<? extends Event> o) {
            int index = findInFixedOrder(o);
            if (index < 0) {
                throw new IllegalStateException(o + "was not in " + fixedOrder);
            }
            return index;
        }

        private int findInFixedOrder(Class<? extends Event> o) {
            for (int i = 0; i < fixedOrder.size(); i++) {
                if (fixedOrder.get(i).isAssignableFrom(o)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class TestCaseEventComparator implements Comparator<TestCaseEvent> {

        @Override
        public int compare(TestCaseEvent a, TestCaseEvent b) {
            int uri = a.getTestCase().getUri().compareTo(b.getTestCase().getUri());
            if (uri != 0) {
                return uri;
            }

            int line = Integer.compare(a.getTestCase().getLine(), b.getTestCase().getLine());
            if(line != 0){
                return line;
            }

            return Long.compare(a.getTimeStamp(), b.getTimeStamp());
        }
    }
}
