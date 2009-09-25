package cuke4duke.internal.language;

import cuke4duke.internal.JRuby;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public abstract class ProgrammingLanguage {
    protected final LanguageMixin languageMixin;

    public ProgrammingLanguage(LanguageMixin languageMixin) {
        this.languageMixin = languageMixin;
    }

    final public RubyArray step_matches(String step_name, String formatted_step_name) {
        return RubyArray.newArray(JRuby.getRuntime(), step_match_list(step_name, formatted_step_name));
    }

    public abstract void load_code_file(String file) throws Throwable;
    protected abstract List<IRubyObject> step_match_list(String step_name, String formatted_step_name);
    public abstract void begin_scenario() throws Throwable;
    public abstract void end_scenario() throws Throwable;
}
