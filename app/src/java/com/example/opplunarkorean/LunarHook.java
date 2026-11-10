package com.example.opplunarkorean;

import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Experimental, narrowly scoped text translation hook for OPPO/ColorOS Calendar.
 * It changes only TextView strings that look like Chinese lunar-date labels.
 */
public final class LunarHook implements IXposedHookLoadPackage {
    private static final String TARGET = "com.coloros.calendar";
    private static final Pattern DAY = Pattern.compile("初([一二三四五六七八九十])|十([一二三四五六七八九])|廿([一二三四五六七八九])|卅([一二])|三十");

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;
        try {
            XposedBridge.hookAllMethods(TextView.class, "setText", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args == null || param.args.length == 0) return;
                    Object value = param.args[0];
                    if (!(value instanceof CharSequence)) return;
                    String original = value.toString();
                    String converted = translateLunar(original);
                    if (!original.equals(converted)) param.args[0] = converted;
                }
            });
            XposedBridge.log("[OppoLunarKorean] Hook active for " + TARGET);
        } catch (Throwable t) {
            XposedBridge.log("[OppoLunarKorean] Hook failed: " + t);
        }
    }

    static String translateLunar(String input) {
        if (input == null || input.isEmpty()) return input;
        // Require a clear lunar marker so ordinary Chinese event titles are not rewritten.
        boolean lunar = input.contains("农历") || input.contains("陰曆") || input.contains("阴历") ||
                input.contains("初") || input.contains("廿") || input.contains("卅") ||
                input.contains("正月") || input.contains("腊月") || input.contains("臘月") || input.contains("冬月");
        if (!lunar) return input;

        String s = input.replace("农历", "음력 ").replace("阴历", "음력 ").replace("陰曆", "음력 ")
                .replace("闰", "윤 ").replace("閏", "윤 ");
        s = s.replace("正月", "1월").replace("冬月", "11월").replace("腊月", "12월").replace("臘月", "12월");
        String[] months = {"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};
        for (int i = 0; i < months.length; i++) s = s.replace(months[i], (i + 1) + "월");

        Matcher m = DAY.matcher(s);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String day;
            if (m.group().equals("三十")) day = "30";
            else if (m.group(1) != null) day = String.valueOf(chineseDigit(m.group(1)));
            else if (m.group(2) != null) day = String.valueOf(10 + chineseDigit(m.group(2)));
            else if (m.group(3) != null) day = String.valueOf(20 + chineseDigit(m.group(3)));
            else day = String.valueOf(30 + chineseDigit(m.group(4)));
            m.appendReplacement(out, Matcher.quoteReplacement(day + "일"));
        }
        m.appendTail(out);
        s = out.toString();
        // Remove an accidental duplicate separator while preserving surrounding layout.
        return s.replace("음력  ", "음력 ").trim();
    }

    private static int chineseDigit(String c) {
        switch (c) {
            case "一": return 1; case "二": return 2; case "三": return 3;
            case "四": return 4; case "五": return 5; case "六": return 6;
            case "七": return 7; case "八": return 8; case "九": return 9; case "十": return 10;
            default: return 0;
        }
    }
}
