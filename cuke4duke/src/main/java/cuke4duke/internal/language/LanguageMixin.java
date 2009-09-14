package cuke4duke.internal.language;

import cuke4duke.internal.jvmclass.ClassAnalyzer;

public interface LanguageMixin {
    void add_hook(String phase, Hook hook);
    void activate(ClassAnalyzer analyzer);
}
