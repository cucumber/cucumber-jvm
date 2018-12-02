package io.cucucumber.jupiter.engine;

import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_CUSTOM_CLASS_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_STRATEGY_PROPERTY_NAME;

public final class Constants {

    public static final String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "cucumber.execution.parallel.enabled";

	static final String PARALLEL_CONFIG_PREFIX = "cucumber.execution.parallel.config.";

	public static final String PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_STRATEGY_PROPERTY_NAME;

	public static final String PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;

	public static final String PARALLEL_CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;

	public static final String PARALLEL_CONFIG_CUSTOM_CLASS_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_CUSTOM_CLASS_PROPERTY_NAME;

	private Constants() {

	}

}
