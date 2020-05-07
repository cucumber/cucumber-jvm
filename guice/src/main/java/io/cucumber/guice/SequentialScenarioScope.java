package io.cucumber.guice;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;

import java.util.HashMap;
import java.util.Map;

class SequentialScenarioScope implements ScenarioScope {

    private Map<Key<?>, Object> scenarioValues = null;

    /**
     * Scopes a provider. The returned provider returns objects from this scope.
     * If an object does not exist in this scope, the provider can use the given
     * unscoped provider to retrieve one.
     * <p>
     * Scope implementations are strongly encouraged to override
     * {@link Object#toString} in the returned provider and include the backing
     * provider's {@code toString()} output.
     *
     * @param  key      binding key
     * @param  unscoped locates an instance when one doesn't already exist in
     *                  this scope.
     * @return          a new provider which only delegates to the given
     *                  unscoped provider when an instance of the requested
     *                  object doesn't already exist in this scope
     */
    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return () -> {
            if (scenarioValues == null) {
                throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
            }

            @SuppressWarnings("unchecked")
            T current = (T) scenarioValues.get(key);
            if (current == null && !scenarioValues.containsKey(key)) {
                current = unscoped.get();
                scenarioValues.put(key, current);
            }
            return current;
        };
    }

    @Override
    public void enterScope() {
        checkState(scenarioValues == null, "A scoping block is already in progress");
        scenarioValues = new HashMap<>();
    }

    @Override
    public void exitScope() {
        checkState(scenarioValues != null, "No scoping block in progress");
        scenarioValues = null;
    }

    private void checkState(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }

}
