package cuke4duke.internal.jvmclass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class DefaultJvmTransforms {
    public static Object transformStringToObject(String argument, Locale locale) {
        return argument;
    }

    public static int transformStringToInt(String argument, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(argument).intValue();
    }

    public static Integer transformStringToInteger(String argument, Locale locale) throws ParseException {
        return transformStringToInt(argument, locale);
    }

    public static long transformStringToLongPrimitive(String argument, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(argument).longValue();
    }

    public static Long transformStringToLong(String argument, Locale locale) throws ParseException {
        return transformStringToLongPrimitive(argument, locale);
    }

    public static double transformStringToDoublePrimitive(String argument, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(argument).doubleValue();
    }

    public static Double transformStringToDouble(String argument, Locale locale) throws ParseException {
        return transformStringToDoublePrimitive(argument, locale);
    }

    public static float transformStringToFloatPrimitive(String argument, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(argument).floatValue();
    }

    public static Float transformStringToFloat(String argument, Locale locale) throws ParseException {
        return transformStringToFloatPrimitive(argument, locale);
    }

    public static short transformStringToShortPrimitive(String argument, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(argument).shortValue();
    }

    public static Short transformStringToShort(String argument, Locale locale) throws ParseException {
        return transformStringToShortPrimitive(argument, locale);
    }

    public static byte transformStringToBytePrimitive(String argument, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(argument).byteValue();
    }

    public static Byte transformStringToByte(String argument, Locale locale) throws ParseException {
        return transformStringToBytePrimitive(argument, locale);
    }

    public static char transformStringToChar(String argument, Locale locale) {
        return argument.charAt(0);
    }

    public static Character transformStringToCharacters(String argument, Locale locale) {
        return argument.charAt(0);
    }

    public static BigDecimal transformStringToBigDecimal(String argument, Locale locale) throws ParseException {
        return BigDecimal.valueOf(transformStringToDoublePrimitive(argument, locale));
    }

    public static BigInteger transformStringToBigInteger(String argument, Locale locale) throws ParseException {
        return BigInteger.valueOf(transformStringToLongPrimitive(argument, locale));
    }

    public static boolean transformStringToBooleanPrimitive(String argument, Locale locale) {
        return Boolean.valueOf(argument);
    }

    public static Boolean transformStringToBoolean(String argument, Locale locale) {
        return Boolean.valueOf(argument);
    }
}
