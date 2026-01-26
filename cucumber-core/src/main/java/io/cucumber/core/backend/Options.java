package io.cucumber.core.backend;

public interface Options {

    boolean isGlueHintEnabled();

    int getGlueHintThreshold();

    Class<? extends ObjectFactory> getObjectFactoryClass();

}
