package cucumber.runtime.transformers;

public class LongTransformable extends TransformableWithNumberFormat<Long> {

	@Override
	protected Long doTransform(Number argument) {
		return Long.valueOf(argument.longValue());
	}

}
