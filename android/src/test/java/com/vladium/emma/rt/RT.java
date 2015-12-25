package com.vladium.emma.rt;

import java.io.File;

/**
 * This is just a stub implementation to test the code coverage logic, it should not be used in multi threaded tests.
 */
public class RT {

    private static File lastFile;
    private static Throwable throwable;

    private RT() {
    }

    public static void dumpCoverageData(final File file, final boolean merge, final boolean stopDataCollection) throws Throwable {

        if (throwable != null) {
            throw throwable;
        }

        file.createNewFile();
        lastFile = file;
    }

    public static void throwOnNextInvocation(final Throwable throwable) {
        RT.throwable = throwable;
    }

    public static void resetMock() {
        lastFile = null;
        throwable = null;
    }

    public static File getLastFile() {
        return lastFile;
    }
}

