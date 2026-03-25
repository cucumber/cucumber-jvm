package io.cucumber.core.backend;

import java.net.URI;
import java.util.List;

public interface GlueDiscoveryRequest {

    List<URI> getGlue();

    List<String> getGlueClassNames();

}
