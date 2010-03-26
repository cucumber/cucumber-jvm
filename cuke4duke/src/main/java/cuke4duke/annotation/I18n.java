package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.StepDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface I18n {

    public class AR {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface بفرض {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface متى {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface عندما {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface اذاً {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface ثم {
            public abstract String value();
        }

    }
    public class BG {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Дадено {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Когато {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface То {
            public abstract String value();
        }

    }
    public class CA {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Donat {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Donada {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Atès {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Atesa {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Quan {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Aleshores {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Cal {
            public abstract String value();
        }

    }
    public class CS {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Pokud {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Když {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Pak {
            public abstract String value();
        }

    }
    public class CYGB {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface anrhegediga {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Pryd {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Yna {
            public abstract String value();
        }

    }
    public class DA {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Givet {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Når {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Så {
            public abstract String value();
        }

    }
    public class DE {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Angenommen {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Gegebensei {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Wenn {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dann {
            public abstract String value();
        }

    }
    public class EN {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Given {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface When {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Then {
            public abstract String value();
        }

    }
    public class ENSCOUSE {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Givun {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Youseknowwhenyousegot {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Wun {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Youseknowlikewhen {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dun {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Denyousegotta {
            public abstract String value();
        }

    }
    public class ENAU {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Yaknowhow {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface When {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Yagotta {
            public abstract String value();
        }

    }
    public class ENLOL {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface ICANHAZ {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface WEN {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface DEN {
            public abstract String value();
        }

    }
    public class ENTX {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Givenyall {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Whenyall {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Thenyall {
            public abstract String value();
        }

    }
    public class ES {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dado {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Cuando {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Entonces {
            public abstract String value();
        }

    }
    public class ET {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Eeldades {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kui {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Siis {
            public abstract String value();
        }

    }
    public class FI {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Oletetaan {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kun {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Niin {
            public abstract String value();
        }

    }
    public class FR {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Soit {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Etantdonné {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Quand {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Lorsque {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Lorsqu {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Alors {
            public abstract String value();
        }

    }
    public class HE {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface בהינתן {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface כאשר {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface אז {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface אזי {
            public abstract String value();
        }

    }
    public class HR {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zadan {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zadani {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zadano {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kada {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kad {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Onda {
            public abstract String value();
        }

    }
    public class HU {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Ha {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Majd {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Akkor {
            public abstract String value();
        }

    }
    public class ID {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dengan {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Ketika {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Maka {
            public abstract String value();
        }

    }
    public class IT {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dato {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Quando {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Allora {
            public abstract String value();
        }

    }
    public class JA {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 前提 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface もし {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface ならば {
            public abstract String value();
        }

    }
    public class KO {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 조건 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 먼저 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 만일 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 만약 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 그러면 {
            public abstract String value();
        }

    }
    public class LT {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Duota {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kai {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Tada {
            public abstract String value();
        }

    }
    public class LV {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kad {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Ja {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Tad {
            public abstract String value();
        }

    }
    public class NL {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Gegeven {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Stel {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Als {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dan {
            public abstract String value();
        }

    }
    public class NO {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Gitt {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Når {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Så {
            public abstract String value();
        }

    }
    public class PL {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zakładając {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Jeżeli {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Wtedy {
            public abstract String value();
        }

    }
    public class PT {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dado {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Quando {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Então {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Entao {
            public abstract String value();
        }

    }
    public class RO {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Daca {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Cand {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Atunci {
            public abstract String value();
        }

    }
    public class RORO {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Dacă {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Când {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Atunci {
            public abstract String value();
        }

    }
    public class RU {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Допустим {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Если {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface То {
            public abstract String value();
        }

    }
    public class SK {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Pokiaľ {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Keď {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Tak {
            public abstract String value();
        }

    }
    public class SRCYRL {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Задато {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Задате {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Задати {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Када {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Кад {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Онда {
            public abstract String value();
        }

    }
    public class SRLATN {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zadato {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zadate {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Zatati {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kada {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Kad {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Onda {
            public abstract String value();
        }

    }
    public class SV {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Givet {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface När {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Så {
            public abstract String value();
        }

    }
    public class TR {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Diyelimki {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Eğerki {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Ozaman {
            public abstract String value();
        }

    }
    public class UZ {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Агар {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Унда {
            public abstract String value();
        }

    }
    public class VI {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Biết {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Cho {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Khi {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface Thì {
            public abstract String value();
        }

    }
    public class ZHCN {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 假如 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 当 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 那么 {
            public abstract String value();
        }

    }
    public class ZHTW {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 假設 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 當 {
            public abstract String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @StepDef
        public static @interface 那麼 {
            public abstract String value();
        }

    }
}