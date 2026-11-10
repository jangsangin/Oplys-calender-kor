package com.example.opplunarkorean;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure-Java translator: Chinese lunar-date / solar-term labels -> Korean. */
public final class LunarTranslator {
    private LunarTranslator() {}

    private static final String MONTH = "(?:闰|閏)?(?:十二|十一|十|一|二|三|四|五|六|七|八|九|正|冬|腊|臘)月";
    private static final String DAY = "(?:初(?:十|一|二|三|四|五|六|七|八|九)|二十|三十|廿(?:一|二|三|四|五|六|七|八|九)|卅|十(?:一|二|三|四|五|六|七|八|九))";
    private static final Pattern TOKEN = Pattern.compile("(" + MONTH + ")|(" + DAY + ")");
    private static final Pattern WHOLE = Pattern.compile("^\\s*(?:" + MONTH + ")?(?:" + DAY + ")?\\s*$");
    private static final Pattern STRONG = Pattern.compile("农历|農曆|农曆|阴历|陰曆|初(?:十|一|二|三|四|五|六|七|八|九)|廿|卅|" + MONTH);

    private static final Map<String, String> TERMS = new HashMap<>();
    static {
        String[] t = {
            "立春","입춘","雨水","우수","惊蛰","경칩","驚蟄","경칩","春分","춘분","清明","청명","谷雨","곡우",
            "立夏","입하","小满","소만","小滿","소만","芒种","망종","芒種","망종","夏至","하지","小暑","소서","大暑","대서",
            "立秋","입추","处暑","처서","處暑","처서","白露","백로","秋分","추분","寒露","한로","霜降","상강",
            "立冬","입동","小雪","소설","大雪","대설","冬至","동지","小寒","소한","大寒","대한",
            "春节","설날","春節","설날","除夕","섣달그믐","元宵","정월대보름","元宵节","정월대보름","元宵節","정월대보름",
            "端午","단오","端午节","단오","端午節","단오","七夕","칠석","七夕节","칠석","中秋","추석","中秋节","추석","中秋節","추석",
            "重阳","중양절","重阳节","중양절","重陽節","중양절","腊八","납팔","臘八","납팔","小年","작은설"
        };
        for (int i = 0; i < t.length; i += 2) TERMS.put(t[i], t[i + 1]);
    }

    public static String translate(String input) {
        if (input == null || input.isEmpty()) return input;
        String trimmed = input.trim();
        String term = TERMS.get(trimmed);
        if (term != null) return term;

        boolean whole = !trimmed.isEmpty() && WHOLE.matcher(trimmed).matches();
        if (!whole && !STRONG.matcher(input).find()) return input;

        String s = input.replace("农历", "음력 ").replace("農曆", "음력 ").replace("农曆", "음력 ")
                .replace("阴历", "음력 ").replace("陰曆", "음력 ");
        Matcher m = TOKEN.matcher(s);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String rep = m.group(1) != null ? month(m.group(1)) : day(m.group(2)) + "일";
            m.appendReplacement(out, Matcher.quoteReplacement(rep));
        }
        m.appendTail(out);
        return out.toString().replace("음력  ", "음력 ").replaceAll("월(?=\\d)", "월 ").trim();
    }

    private static String month(String tok) {
        boolean leap = tok.startsWith("闰") || tok.startsWith("閏");
        String name = (leap ? tok.substring(1) : tok);
        name = name.substring(0, name.length() - 1); // drop 月
        int n;
        switch (name) {
            case "正": case "一": n = 1; break;
            case "二": n = 2; break; case "三": n = 3; break; case "四": n = 4; break;
            case "五": n = 5; break; case "六": n = 6; break; case "七": n = 7; break;
            case "八": n = 8; break; case "九": n = 9; break; case "十": n = 10; break;
            case "十一": case "冬": n = 11; break;
            case "十二": case "腊": case "臘": n = 12; break;
            default: return tok;
        }
        return (leap ? "윤" : "") + n + "월";
    }

    private static String day(String tok) {
        if (tok.equals("二十")) return "20";
        if (tok.equals("三十") || tok.equals("卅")) return "30";
        char last = tok.charAt(tok.length() - 1);
        int d = digit(last);
        switch (tok.charAt(0)) {
            case '初': return String.valueOf(d);
            case '十': return String.valueOf(10 + d);
            case '廿': return String.valueOf(20 + d);
            default: return tok;
        }
    }

    private static int digit(char c) {
        switch (c) {
            case '一': return 1; case '二': return 2; case '三': return 3; case '四': return 4;
            case '五': return 5; case '六': return 6; case '七': return 7; case '八': return 8;
            case '九': return 9; case '十': return 10; default: return 0;
        }
    }
}
