package com.example.opplunarkorean;

import android.graphics.Canvas;
import android.text.Spanned;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.HashSet;
import java.util.Set;

/**
 * Translates Chinese lunar-date labels in OPPO/ColorOS Calendar to Korean.
 * Hooks TextView.setText and the Canvas text-drawing methods, because the calendar
 * grid is usually drawn by a custom View through Canvas instead of a TextView.
 */
public final class LunarHook implements IXposedHookLoadPackage {
    private static final String TAG = "[OppoLunarKorean] ";
    private static final Set<String> SEEN = new HashSet<>();

    private static boolean isTarget(String pkg) {
        return "com.coloros.calendar".equals(pkg) || "com.oplus.calendar".equals(pkg);
    }

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!isTarget(lpparam.packageName)) return;
        XposedBridge.log(TAG + "loaded in " + lpparam.packageName);
        hookTextView();
        hookCanvas(Canvas.class);
        try {
            hookCanvas(Class.forName("android.graphics.BaseRecordingCanvas"));
        } catch (Throwable ignored) { }
        try {
            hookCanvas(Class.forName("android.graphics.RecordingCanvas"));
        } catch (Throwable ignored) { }
    }

    private static void hookTextView() {
        try {
            XposedBridge.hookAllMethods(TextView.class, "setText", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args == null || param.args.length == 0) return;
                    Object v = param.args[0];
                    if (!(v instanceof CharSequence) || v instanceof Spanned) return;
                    String o = v.toString();
                    String c = convert(o, "setText");
                    if (c != o) param.args[0] = c;
                }
            });
            XposedBridge.log(TAG + "TextView.setText hooked");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "TextView hook failed: " + t);
        }
    }

    private static void hookCanvas(Class<?> cls) {
        try {
            // drawText(String|CharSequence|char[], ...) variants
            XposedBridge.hookAllMethods(cls, "drawText", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    rewriteRange(param.args, 0, 1, 2, "drawText");
                }
            });
            // drawTextRun(CharSequence, start, end, contextStart, contextEnd, x, y, rtl, paint)
            XposedBridge.hookAllMethods(cls, "drawTextRun", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    Object[] a = param.args;
                    if (a == null || a.length < 5 || !(a[0] instanceof CharSequence)) return;
                    if (!(a[1] instanceof Integer) || !(a[2] instanceof Integer)) return;
                    int s = (Integer) a[1], e = (Integer) a[2];
                    CharSequence full = (CharSequence) a[0];
                    if (s < 0 || e > full.length() || s > e) return;
                    String o = full.subSequence(s, e).toString();
                    String c = convert(o, "drawTextRun");
                    if (c == o) return;
                    a[0] = c; a[1] = 0; a[2] = c.length();
                    if (a[3] instanceof Integer) a[3] = 0;
                    if (a[4] instanceof Integer) a[4] = c.length();
                }
            });
            XposedBridge.log(TAG + "Canvas text hooks on " + cls.getName());
        } catch (Throwable t) {
            XposedBridge.log(TAG + "Canvas hook failed on " + cls.getName() + ": " + t);
        }
    }

    /** Handles drawText(String, x, y, paint) and drawText(CharSequence|String, start, end, x, y, paint). */
    private static void rewriteRange(Object[] a, int textIdx, int startIdx, int endIdx, String where) {
        if (a == null || a.length == 0) return;
        Object t = a[textIdx];
        if (!(t instanceof String) && !(t instanceof CharSequence)) return; // char[] is not handled
        boolean ranged = a.length >= 3 && a[startIdx] instanceof Integer && a[endIdx] instanceof Integer;
        CharSequence cs = (CharSequence) t;
        int s = 0, e = cs.length();
        if (ranged) {
            s = (Integer) a[startIdx]; e = (Integer) a[endIdx];
            if (s < 0 || e > cs.length() || s > e) return;
        }
        String o = cs.subSequence(s, e).toString();
        String c = convert(o, where);
        if (c == o) return;
        a[textIdx] = c;
        if (ranged) { a[startIdx] = 0; a[endIdx] = c.length(); }
    }

    /** Returns the same instance when nothing changed. Logs unconverted CJK strings once for debugging. */
    private static String convert(String o, String where) {
        if (o == null || o.isEmpty() || o.length() > 60) return o;
        String c = LunarTranslator.translate(o);
        if (!c.equals(o)) {
            logOnce("OK   " + where + ": " + o + " -> " + c);
            return c;
        }
        if (hasCjk(o)) logOnce("SKIP " + where + ": " + o);
        return o;
    }

    private static boolean hasCjk(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 0x4E00 && ch <= 0x9FFF) return true;
        }
        return false;
    }

    private static void logOnce(String msg) {
        synchronized (SEEN) {
            if (SEEN.size() > 300 || !SEEN.add(msg)) return;
        }
        XposedBridge.log(TAG + msg);
    }
}
