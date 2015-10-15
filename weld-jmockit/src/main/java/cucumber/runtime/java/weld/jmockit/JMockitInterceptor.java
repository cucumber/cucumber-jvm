package cucumber.runtime.java.weld.jmockit;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import mockit.integration.internal.TestRunnerDecorator;
import mockit.internal.state.SavePoint;
import mockit.internal.state.TestRun;

@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@WithJMockit
public final class JMockitInterceptor extends TestRunnerDecorator {

	private final ThreadLocal<SavePoint> savePoint = new ThreadLocal<SavePoint>();
	
	@AroundInvoke
	public Object intercept(final InvocationContext ctx) throws Exception {
		final Object result;
		this.beforeMethod(ctx);
		try {
			result = ctx.proceed();
		} finally {
			this.afterMethod(ctx);
		}
		return result;
	}

	private void afterMethod(final InvocationContext ctx) {

	      final SavePoint testMethodSavePoint = savePoint.get();

	      if (testMethodSavePoint == null) {
	         return;
	      }

	      TestRun.enterNoMockingZone();
	      
	      shouldPrepareForNextTest = true;
	      savePoint.set(null);

	      TestRun.finishCurrentTestExecution();
	}

	private void beforeMethod(final InvocationContext ctx) {
		
		TestRun.clearNoMockingZone();

		TestRun.enterNoMockingZone();

		try {
			updateTestClassState(ctx.getTarget(), ctx.getTarget().getClass());
			TestRun.setRunningIndividualTest(ctx.getTarget());
			final SavePoint testMethodSavePoint = new SavePoint();
			savePoint.set(testMethodSavePoint);
			if (shouldPrepareForNextTest) {
				TestRun.prepareForNextTest();
				shouldPrepareForNextTest = false;
			}
			createInstancesForTestedFields(ctx.getTarget(), false);
		} finally {
			TestRun.exitNoMockingZone();
		}

	}

}
