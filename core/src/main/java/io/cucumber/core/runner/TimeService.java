package io.cucumber.core.runner;

import java.time.Instant;

public interface TimeService {
    
    Instant timeInstant();

    TimeService SYSTEM = new TimeService() {

        @Override
        public Instant timeInstant() {
            return Instant.now();
        }
    };

}
