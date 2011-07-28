package cucumber.runtime.transformers;

public class DoubleTransformable extends TransformableWithNumberFormat<Double> {

	@Override
	protected Double doTransform(Number transform) {
		return Double.valueOf(transform.doubleValue());
	}

}
