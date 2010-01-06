package cuke4duke.internal.jvmclass;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultJvmTransforms {
    public static Object transformStringToObject(String argument) {
        return argument;
    }

    public static int transformStringToInt(String argument) {
        return Integer.valueOf(argument);
    }

    public static Integer transformStringToInteger(String argument) {
        return Integer.valueOf(argument);
    }

    public static long transformStringToLongPrimitive(String argument) {
        return Long.valueOf(argument);
    }

    public static Long transformStringToLong(String argument) {
        return Long.valueOf(argument);
    }

    public static double transformStringToDoublePrimitive(String argument) {
        return Double.valueOf(argument);
    }

    public static Double transformStringToDouble(String argument) {
        return Double.valueOf(argument);
    }

    public static float transformStringToFloatPrimitive(String argument) {
        return Float.valueOf(argument);
    }

    public static Float transformStringToFloat(String argument) {
        return Float.valueOf(argument);
    }

    public static short transformStringToShortPrimitive(String argument) {
        return Short.valueOf(argument);
    }

    public static Short transformStringToShort(String argument) {
        return Short.valueOf(argument);
    }

    public static byte transformStringToBytePrimitive(String argument) {
        return Byte.valueOf(argument);
    }

    public static Byte transformStringToByte(String argument) {
        return Byte.valueOf(argument);
    }

    public static char transformStringToChar(String argument) {
        return Character.valueOf(argument.charAt(0));
    }

    public static Character transformStringToCharacters(String argument) {
        return Character.valueOf(argument.charAt(0));
    }

    public static BigDecimal transformStringToBigDecimal(String argument) {
        return BigDecimal.valueOf(Double.valueOf(argument));
    }

    public static BigInteger transformStringToBigInteger(String argument) {
        return BigInteger.valueOf(Long.valueOf(argument));
    }

    public static boolean transformStringToBooleanPrimitive(String argument) {
        return Boolean.valueOf(argument);
    }

    public static Boolean transformStringToBoolean(String argument) {
        return Boolean.valueOf(argument);
    }
}
