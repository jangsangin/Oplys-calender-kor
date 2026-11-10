package de.robv.android.xposed;
import java.util.Set;
public final class XposedBridge {
    private XposedBridge() {}
    public static void log(String text) {}
    public static void log(Throwable t) {}
    // Return type must match the real API exactly (java.util.Set), or the JVM throws NoSuchMethodError.
    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) { return null; }
}
