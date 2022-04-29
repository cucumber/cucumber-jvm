package io.cucumber.lambda;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "7.4.0")
public interface StepDefinitionFunction {

    @FunctionalInterface
    interface C0A0 extends StepDefinitionFunction {
        StepDefinitionBody.A0 accept();

    }

    @FunctionalInterface
    interface C1A0<Context> extends StepDefinitionFunction {
        StepDefinitionBody.A0 accept(Context context);

    }

    @FunctionalInterface
    interface C1A1<Context, T1> extends StepDefinitionFunction {
        StepDefinitionBody.A1<T1> accept(Context context);

    }

    @FunctionalInterface
    interface C1A2<Context, T1, T2> extends StepDefinitionFunction {
        StepDefinitionBody.A2<T1, T2> accept(Context context);

    }

    @FunctionalInterface
    interface C1A3<Context, T1, T2, A3> extends StepDefinitionFunction {
        StepDefinitionBody.A3<T1, T2, A3> accept(Context context);

    }

    @FunctionalInterface
    interface C1A4<Context, T1, T2, T3, T4> extends StepDefinitionFunction {
        StepDefinitionBody.A4<T1, T2, T3, T4> accept(Context context);

    }

    @FunctionalInterface
    interface C1A5<Context, T1, T2, T3, T4, T5> extends StepDefinitionFunction {
        StepDefinitionBody.A5<T1, T2, T3, T4, T5> accept(Context context);

    }

    @FunctionalInterface
    interface C1A6<Context, T1, T2, T3, T4, T5, T6> extends StepDefinitionFunction {
        StepDefinitionBody.A6<T1, T2, T3, T4, T5, T6> accept(Context context);

    }

    @FunctionalInterface
    interface C1A7<Context, T1, T2, T3, T4, T5, T6, T7> extends StepDefinitionFunction {
        StepDefinitionBody.A7<T1, T2, T3, T4, T5, T6, T7> accept(Context context);

    }

    @FunctionalInterface
    interface C1A8<Context, T1, T2, T3, T4, T5, T6, T7, T8> extends StepDefinitionFunction {
        StepDefinitionBody.A8<T1, T2, T3, T4, T5, T6, T7, T8> accept(Context context);

    }

    @FunctionalInterface
    interface C1A9<Context, T1, T2, T3, T4, T5, T6, T7, T8, T9> extends StepDefinitionFunction {
        StepDefinitionBody.A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> accept(Context context);

    }

    interface StepDefinitionBody {

        @FunctionalInterface
        interface A0 extends StepDefinitionBody {
            void accept() throws Throwable;

        }

        @FunctionalInterface
        interface A1<T1> extends StepDefinitionBody {
            void accept(T1 t1) throws Throwable;

        }

        @FunctionalInterface
        interface A2<T1, T2> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2) throws Throwable;

        }

        @FunctionalInterface
        interface A3<T1, T2, T3> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3) throws Throwable;

        }

        @FunctionalInterface
        interface A4<T1, T2, T3, T4> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable;

        }

        @FunctionalInterface
        interface A5<T1, T2, T3, T4, T5> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable;

        }

        @FunctionalInterface
        interface A6<T1, T2, T3, T4, T5, T6> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) throws Throwable;

        }

        @FunctionalInterface
        interface A7<T1, T2, T3, T4, T5, T6, T7> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) throws Throwable;

        }

        @FunctionalInterface
        interface A8<T1, T2, T3, T4, T5, T6, T7, T8> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) throws Throwable;

        }

        @FunctionalInterface
        interface A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends StepDefinitionBody {
            void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) throws Throwable;

        }

    }

}
