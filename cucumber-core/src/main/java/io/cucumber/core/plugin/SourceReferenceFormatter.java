package io.cucumber.core.plugin;

import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.SourceReference;

import java.util.Optional;

final class SourceReferenceFormatter {

    SourceReferenceFormatter() {
    }

    Optional<String> format(SourceReference sourceReference) {
        if (sourceReference.getJavaMethod().isPresent()) {
            return sourceReference.getJavaMethod()
                    .map(javaMethod -> String.format(
                        "%s#%s(%s)",
                        javaMethod.getClassName(),
                        javaMethod.getMethodName(),
                        String.join(",", javaMethod.getMethodParameterTypes())));
        }
        if (sourceReference.getJavaStackTraceElement().isPresent()) {
            return sourceReference.getJavaStackTraceElement()
                    .map(javaStackTraceElement -> String.format(
                        "%s#%s(%s%s)",
                        javaStackTraceElement.getClassName(),
                        javaStackTraceElement.getMethodName(),
                        javaStackTraceElement.getFileName(),
                        sourceReference.getLocation().map(Location::getLine).map(line -> ":" + line).orElse("")));
        }
        if (sourceReference.getUri().isPresent()) {
            return sourceReference.getUri()
                    .map(uri -> uri + sourceReference.getLocation()
                            .map(location -> ":" + location.getLine())
                            .orElse(""));
        }
        return Optional.empty();
    }
}
