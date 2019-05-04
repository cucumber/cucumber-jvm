package io.cucumber.core.runner;

import java.time.Instant;

public interface TimeService {
    
    //gazler
//    long time();
//    long timeMillis();
    Instant timeInstant();

    TimeService SYSTEM = new TimeService() {
//        @Override
//        public long time() {
//            return System.nanoTime();
//        }
//
//        @Override
//        public long timeMillis() {
//            return System.currentTimeMillis();
//        }

        @Override
        public Instant timeInstant() {
            return Instant.now();
        }
    };

}
