package io.cucumber.core.cli;

public final class MainDemo {
    
    private MainDemo(){
        /* no-op */
    }

    public static void main(String[] args) {
        // Main.main("--i18n");
        // Main.main("--i18n", "help");
        Main.main("--i18n-keywords", "tlh");
    }

}
