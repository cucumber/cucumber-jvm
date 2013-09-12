package cucumber.runtime.rhino;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import gherkin.formatter.model.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class RhinoHooksTest {

	private static final String[] NO_TAG = { };
	private static final String[] TAG = { "@bellies" };
	private static final String[] TAGS = { "@tag1", "@tag2" };
	
	private static final int DEFAULT_ORDER = 1000;
	private static final int DEFAULT_TIMEOUT = 0;
	
	private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	private final ResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

	@Mock private RuntimeGlue glue;
	
	private ArgumentCaptor<HookDefinition> beforeHookCaptor;
	private ArgumentCaptor<HookDefinition> afterHookCaptor;

	@Before
	public void startCapturingHooksArguments() {
    	// given
    	beforeHookCaptor = ArgumentCaptor.forClass(HookDefinition.class);
    	afterHookCaptor = ArgumentCaptor.forClass(HookDefinition.class);
    	doNothing().when(glue).addBeforeHook(beforeHookCaptor.capture());
    	doNothing().when(glue).addAfterHook(afterHookCaptor.capture());
	}
	
    @Test
    public void shouldCallAddBeforeAndAfterHook() throws IOException {
    	// when
    	RhinoBackend jsBackend = new RhinoBackend(resourceLoader);
    	jsBackend.loadGlue(glue, Collections.singletonList("cucumber/runtime/rhino_hooks"));
    	List<HookDefinition> beforeHooks = beforeHookCaptor.getAllValues();
    	List<HookDefinition> afterHooks = afterHookCaptor.getAllValues();
    	
    	// then
    	assertHooks(beforeHooks.get(0), afterHooks.get(0), NO_TAG, DEFAULT_ORDER, DEFAULT_TIMEOUT);
    	assertHooks(beforeHooks.get(1), afterHooks.get(1), TAG, DEFAULT_ORDER, DEFAULT_TIMEOUT);
    	assertHooks(beforeHooks.get(2), afterHooks.get(2), TAGS, DEFAULT_ORDER, DEFAULT_TIMEOUT);
    	assertHooks(beforeHooks.get(3), afterHooks.get(3), TAGS, DEFAULT_ORDER, 300);
    	assertHooks(beforeHooks.get(4), afterHooks.get(4), TAGS, 10, DEFAULT_TIMEOUT);
    	assertHooks(beforeHooks.get(5), afterHooks.get(5), TAGS, 20, 600);
    }

    @Test
    public void shouldFailWithTimeout() throws Throwable {
    	// when
    	RhinoBackend jsBackend = new RhinoBackend(resourceLoader);
    	jsBackend.loadGlue(glue, Collections.singletonList("cucumber/runtime/rhino_hooks_timeout"));
    	List<HookDefinition> beforeHooks = beforeHookCaptor.getAllValues();
    	
    	try {
			beforeHooks.get(0).execute(null);
		} catch (Exception e) {
			// then 
			assertThat(e.getCause(), instanceOf(InterruptedException.class));
			return;
		}
    	
    	fail(InterruptedException.class.getSimpleName() + " expected");
    }

	private void assertHooks(HookDefinition beforeHook, HookDefinition afterHook, String[] tags, int order, int timeout) {
		assertHook(beforeHook, tags, order, timeout);
		assertHook(afterHook, tags, order, timeout);
	}

	private void assertHook(HookDefinition hookDefinition, String[] tagExprs, int order, int timeout) {
		assertThat(hookDefinition, instanceOf(RhinoHookDefinition.class));
		
		RhinoHookDefinition rhinoHook = (RhinoHookDefinition) hookDefinition;
		
		List<Tag> tags = new ArrayList<Tag>();
		
		for (String tagExpr : tagExprs) {
			tags.add(new Tag(tagExpr, null));
		}
		
		assertTrue(rhinoHook.getTagExpression().evaluate(tags));
		assertThat(rhinoHook.getOrder(), equalTo(order));
		assertThat(rhinoHook.getTimeout(), equalTo(timeout));
	}

}