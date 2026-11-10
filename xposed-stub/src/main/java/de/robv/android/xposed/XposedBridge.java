package de.robv.android.xposed;
public final class XposedBridge {
    private XposedBridge() {}
    public static void log(String text) {}
    public static void log(Throwable t) {}
    public static Object hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) { return null; }
}
