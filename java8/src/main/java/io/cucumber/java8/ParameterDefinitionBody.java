package io.cucumber.java8;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface ParameterDefinitionBody {

    @FunctionalInterface
    interface A1<R> extends ParameterDefinitionBody {

        R accept(String p1) throws Throwable;

    }

    @FunctionalInterface
    interface A2<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2) throws Throwable;

    }

    @FunctionalInterface
    interface A3<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3) throws Throwable;

    }

    @FunctionalInterface
    interface A4<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3, String p4) throws Throwable;

    }

    @FunctionalInterface
    interface A5<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3, String p4, String p5) throws Throwable;

    }

    @FunctionalInterface
    interface A6<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3, String p4, String p5, String p6) throws Throwable;

    }

    @FunctionalInterface
    interface A7<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3, String p4, String p5, String p6, String p7) throws Throwable;

    }

    @FunctionalInterface
    interface A8<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8)
                throws Throwable;

    }

    @FunctionalInterface
    interface A9<R> extends ParameterDefinitionBody {

        R accept(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9)
                throws Throwable;

    }

}
