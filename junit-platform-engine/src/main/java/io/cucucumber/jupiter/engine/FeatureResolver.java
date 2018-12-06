package io.cucucumber.jupiter.engine;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;

import static io.cucucumber.jupiter.engine.Classloaders.getDefaultClassLoader;
import static io.cucucumber.jupiter.engine.FeatureSource.fromClassPathResource;
import static io.cucucumber.jupiter.engine.FeatureSource.fromFile;
import static java.util.Collections.singletonList;

final class FeatureResolver {

    private final TestDescriptor engineDescriptor;

    private FeatureResolver(TestDescriptor engineDescriptor) {
        this.engineDescriptor = engineDescriptor;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor) {
        return new FeatureResolver(engineDescriptor);
    }

    void resolveFile(DirectorySelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getRawPath()))
            .forEach(feature -> resolveFeature(feature, fromFile()));
    }

    void resolveFile(FileSelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getRawPath()))
            .forEach(feature -> resolveFeature(feature, fromFile()));
    }

    void resolvePackageResource(PackageSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getPackageName().replace('.', '/')))
            .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
    }

    void resolveClassPathResource(ClasspathResourceSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getClasspathResourceName()))
            .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
    }

    void resolveClassPathRoot(ClasspathRootSelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getClasspathRoot().getPath()))
            .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
    }

    void resolveUri(UriSelector uriSelector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(uriSelector.getUri().getPath()))
            .forEach(feature -> resolveFeature(feature, fromFile()));
    }

    private void resolveFeature(CucumberFeature feature, FeatureSource source) {
        TestDescriptor featureDescriptor = FeatureDescriptor.create(feature, source, engineDescriptor);
        recursivelyAdd(featureDescriptor, engineDescriptor);
    }

    private void recursivelyAdd(TestDescriptor descriptor, TestDescriptor parent) {
        if (!parent.findByUniqueId(descriptor.getUniqueId()).isPresent()) {
            parent.addChild(descriptor);
        }
        descriptor.getChildren().forEach(child -> recursivelyAdd(child, descriptor));
    }
}
