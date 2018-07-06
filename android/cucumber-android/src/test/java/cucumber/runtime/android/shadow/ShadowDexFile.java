package cucumber.runtime.android.shadow;

import dalvik.system.DexFile;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(DexFile.class)
public class ShadowDexFile {

    private Enumeration<String> entries;

    @Implementation
    public Enumeration<String> entries() {
        return entries;
    }

    public void setEntries(final Collection<String> entries) {
        this.entries = Collections.enumeration(entries);
    }
}
