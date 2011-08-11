package cucumber.runtime.transformers;

import gherkin.formatter.Argument;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/**
 * 
 * Class for transforming argument to a certain type using a Locale
 *
 */
public class Transformer {
	private Map<Class<?>, Transformable<?>> transformables;

	@SuppressWarnings("unchecked")
	public <T> T transform(Argument argument, Class<?> clazz, Locale locale) {
		Transformable<?> transformable = getTransformable(clazz);
		if (transformable == null) {
			throw new TransformationException("Can't transform " + argument.getVal() + " to: " + clazz.getName() + ". No transformable found.");
		}
		return (T) transformable.transform(argument.getVal(), locale);
	}

	private Transformable<?> getTransformable(Class<?> clazz) {
		return getTransformables().get(clazz);
	}

	public Map<Class<?>, Transformable<?>> getTransformables() {
		if (this.transformables == null) {
			this.transformables = registerDefaultTransformables();
		}
		return this.transformables;
	}

	protected Map<Class<?>, Transformable<?>> registerDefaultTransformables() {
		HashMap<Class<?>, Transformable<?>> hashMap = new HashMap<Class<?>, Transformable<?>>();
		hashMap.put(String.class, new StringTransformable());
		hashMap.put(Date.class, new DateTransformable());
		hashMap.put(BigDecimal.class, new BigDecimalTransformable());
		hashMap.put(BigIntegerTransformable.class, new BigIntegerTransformable());
		BooleanTransformable booleanTransformable = new BooleanTransformable();
		hashMap.put(Boolean.TYPE, booleanTransformable);
		hashMap.put(Boolean.class, booleanTransformable);
		ByteTransformable byteTransformable = new ByteTransformable();
		hashMap.put(Byte.TYPE, byteTransformable);
		hashMap.put(Byte.class, byteTransformable);
		CharacterTransformable characterTransformable = new CharacterTransformable();
		hashMap.put(Character.TYPE, characterTransformable);
		hashMap.put(Character.class, characterTransformable);
		DoubleTransformable doubleTransformable = new DoubleTransformable();
		hashMap.put(Double.TYPE, doubleTransformable);
		hashMap.put(Double.class, doubleTransformable);
		FloatTransformable floatTransformable = new FloatTransformable();
		hashMap.put(Float.TYPE, floatTransformable);
		hashMap.put(Float.class, floatTransformable);
		IntegerTransformable integerTransformable = new IntegerTransformable();
		hashMap.put(Integer.TYPE, integerTransformable);
		hashMap.put(Integer.class, integerTransformable);
		LongTransformable longTransformable = new LongTransformable();
		hashMap.put(Long.TYPE, longTransformable);
		hashMap.put(Long.class, longTransformable);
		ShortTransformable shortTransformable = new ShortTransformable();
		hashMap.put(Short.TYPE, shortTransformable);
		hashMap.put(Short.class, shortTransformable);
		return hashMap;
	}
	
	public void addTransformable(Class<?> clazz, Transformable<?> transformable) {
		getTransformables().put(clazz, transformable);
	}

	public void setTransformables(Map<Class<?>, Transformable<?>> transformables) {
		this.transformables = transformables;
	}
}
