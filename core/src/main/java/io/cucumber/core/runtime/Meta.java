package io.cucumber.core.runtime;

import io.cucumber.messages.Messages;

class Meta {
    static Messages.Meta makeMeta() {
        String version = Meta.class.getPackage().getImplementationVersion();
        if(version == null) {
            // Development version
            version = "unreleased";
        }
        return Messages.Meta.newBuilder()
            .setProtocolVersion(Messages.class.getPackage().getImplementationVersion())
            .setRuntime(Messages.Meta.Product.newBuilder()
                .setName(System.getProperty("java.vendor"))
                .setVersion(System.getProperty("java.version"))
            )
            .setImplementation(Messages.Meta.Product.newBuilder()
                .setName("cucumber-jvm")
                .setVersion(version)
            )
            .setOs(Messages.Meta.Product.newBuilder()
                .setName(System.getProperty("os.name"))
            )
            .setCpu(Messages.Meta.Product.newBuilder()
                .setName(System.getProperty("os.arch"))
            )
            .build();
    }
}
