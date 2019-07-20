package io.cucumber.core.options;

public final class Constants {

    /**
     * File name of cucumber properties file: {@value}
     */
    public static final String CUCUMBER_PROPERTIES_FILE_NAME = "cucumber.properties";

    /**
     * Property name used to pass command line options: {@value}
     * <p>
     * When available it is recommended to use a property based alternative.
     *
     * @see RuntimeOptionsParser
     */
    public static final String CUCUMBER_OPTIONS_PROPERTY_NAME = "cucumber.options";
    /**
     * Property name used to select a specific object factory implementation: {@value}
     *
     * @see io.cucumber.core.backend.ObjectFactoryServiceLoader
     */
    public static final String CUCUMBER_OBJECT_FACTORY_PROPERTY_NAME = "cucumber.object-factory";


    private Constants() {

    }
}
