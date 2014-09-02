package cucumber.api.java8;

public interface StepdefBody {
    @FunctionalInterface
    public static interface A0 extends StepdefBody {
        void accept();
    }

    @FunctionalInterface
    public static interface A1<T1> extends StepdefBody {
        void accept(T1 p1);
    }

    @FunctionalInterface
    public static interface A2<T1, T2> extends StepdefBody {
        void accept(T1 p1, T2 p2);
    }

    @FunctionalInterface
    public static interface A3<T1, T2, T3> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3);
    }

    @FunctionalInterface
    public static interface A4<T1, T2, T3, T4> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4);
    }

    @FunctionalInterface
    public static interface A5<T1, T2, T3, T4, T5> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5);
    }

    @FunctionalInterface
    public static interface A6<T1, T2, T3, T4, T5, T6> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6);
    }

    @FunctionalInterface
    public static interface A7<T1, T2, T3, T4, T5, T6, T7> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6, T7 p7);
    }

    @FunctionalInterface
    public static interface A8<T1, T2, T3, T4, T5, T6, T7, T8> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6, T7 p7, T8 p8);
    }

    @FunctionalInterface
    public static interface A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6, T7 p7, T8 p8, T9 p9);
    }
}
