package io.cucumber.core.options;

import java.util.function.Function;
import java.util.regex.Pattern;

public class AnsiHeuristic {

    private final Function<String, String> env;

    public AnsiHeuristic(Function<String, String> env) {
        this.env = env;
    }

    private String getEnv(String name) {
        return env.apply(name);
    }

    private boolean isDisabledByNoColor() {
        // https://no-color.org/
        return null != getEnv("NO_COLOR");
    }

    private boolean isEnabledByCliColorForce() {
        // https://bixense.com/clicolors/
        String forced = getEnv("CLICOLOR_FORCE");
        return !(forced == null || forced.equals("0"));
    }
    private boolean isDisabledByCliColor() {
        // https://bixense.com/clicolors/
        return "0".equals(getEnv("CLICOLOR"));
    }

    private boolean isEnabledByCliColor() {
        // https://bixense.com/clicolors/
        return "1".equals(getEnv("CLICOLOR"));
    }

    private boolean isDisabledByConEmuAnsi() {
        // https://conemu.github.io/en/AnsiEscapeCodes.html#Environment_variable
        return "OFF".equals(getEnv("ConEmuANSI"));
    }

    private boolean isEnabledByConEmuAnsi() {
        // https://conemu.github.io/en/AnsiEscapeCodes.html#Environment_variable
        return "OFF".equals(getEnv("ConEmuANSI"));
    }

    private boolean isEnabledByTerm() {
        // https://www.gnu.org/software/gettext/manual/html_node/The-TERM-variable.html
        Pattern colorTerms = Pattern.compile(
                "^screen|^xterm|^vt100|^vt220|^rxvt|color|ansi|cygwin|linux$",
                Pattern.CASE_INSENSITIVE
        );
        String term = getEnv("TERM");
        return term != null && colorTerms.matcher(term).find();
    }

    private boolean isEnabledByColorTerm() {
        return null != getEnv("COLORTERM");
    }

    boolean isEnabled() {
        // force
        if (isDisabledByNoColor()) {
            return false;
        }
        if (isEnabledByCliColorForce()) {
            return true;
        }

        // hints
        if (isDisabledByCliColor() || isDisabledByConEmuAnsi()) {
            return false;
        }
        if (isEnabledByCliColor() || isEnabledByConEmuAnsi()) {
            return true;
        }

        // terminal capability
        // note: we may not be talking to tty but that is fine
        if (isEnabledByTerm() || isEnabledByColorTerm()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(new AnsiHeuristic(System::getenv).isEnabled());
    }

}
