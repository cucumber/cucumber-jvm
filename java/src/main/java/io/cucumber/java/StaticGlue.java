package io.cucumber.java;

import cucumber.api.java.ObjectFactory;
import io.cucumber.core.stepexpression.TypeRegistry;

public class StaticGlue {



    private static interface GlueProvider {

        StaticGlue loadGlue(String glue_paths);
    }

    private static class JavaGlueProvider implements GlueProvider{

        // same constructor as JavaBackend

        @Override
        public StaticGlue loadGlue(String glue_paths) {
            // create glue instance

            // add glue methods

            return null;
        }
    }


    private static class Main {

        public void main(){

            StaticGlue staticGlue = new JavaGlueProvider().loadGlue("glue paths");

            staticGlue.buildWorld();



        }


    }

    private void buildWorld() {
    }


    private TypeRegistry typeRegistry;
    private ObjectFactory objectFactory;




}


