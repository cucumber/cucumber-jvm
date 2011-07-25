package cucumber.runtime.transformers;

import gherkin.formatter.Argument;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Transformer {
	private Map<Class<?>, Transformable<?>> transformables;

	@SuppressWarnings("unchecked")
	public <T> T transform(Argument argument, Class<?> clazz, Locale locale) {
		return (T) getTransformable(clazz).transform(argument.getVal(), locale);
	}

	private Transformable<?> getTransformable(Class<?> clazz) {
		return getTransformables().get(clazz);
	}

	public Map<Class<?>, Transformable<?>> getTransformables() {
		if (this.transformables == null) {
			this.transformables = createStandardTransformables();
		}
		return this.transformables;
	}

	protected Map<Class<?>, Transformable<?>> createStandardTransformables() {
		HashMap<Class<?>, Transformable<?>> hashMap = new HashMap<Class<?>, Transformable<?>>();
		BooleanTransformable booleanTransformable = new BooleanTransformable();
		hashMap.put(Boolean.TYPE, booleanTransformable);
		hashMap.put(Boolean.class, booleanTransformable);
		return hashMap;
	}

	public void setTransformables(Map<Class<?>, Transformable<?>> transformables) {
		this.transformables = transformables;
	}
}
