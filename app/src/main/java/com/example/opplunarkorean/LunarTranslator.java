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
    /** true: 중국 전용 기념일은 화면에서 완전히 숨김 / false: 한국어로 번역해서 표시 */
    private static final boolean HIDE_CHINA_ONLY = true;
    private static void put(String ko, String... zh) { for (String z : zh) TERMS.put(z, ko); }
    private static void putChina(String ko, String... zh) { put(HIDE_CHINA_ONLY ? "" : ko, zh); }
    static {
        // --- 한국과 같은 음력 명절 (중국식 이름 -> 한국식 이름) ---
        put("설날", "春节", "春節", "农历新年", "農曆新年");
        put("섣달그믐", "除夕");
        put("정월대보름", "元宵", "元宵节", "元宵節", "上元节", "上元節");
        put("삼짇날", "上巳节", "上巳節");
        put("부처님오신날", "浴佛节", "浴佛節", "佛诞", "佛誕");
        put("단오", "端午", "端午节", "端午節");
        put("칠석", "七夕", "七夕节", "七夕節");
        put("백중", "中元节", "中元節");
        put("추석", "中秋", "中秋节", "中秋節");
        put("중양절", "重阳", "重阳节", "重陽節", "重陽");
        put("한식", "寒食节", "寒食節", "寒食");
        // --- 24절기 ---
        put("입춘", "立春"); put("우수", "雨水"); put("경칩", "惊蛰", "驚蟄"); put("춘분", "春分");
        put("청명", "清明", "清明节", "清明節"); put("곡우", "谷雨");
        put("입하", "立夏"); put("소만", "小满", "小滿"); put("망종", "芒种", "芒種"); put("하지", "夏至");
        put("소서", "小暑"); put("대서", "大暑");
        put("입추", "立秋"); put("처서", "处暑", "處暑"); put("백로", "白露"); put("추분", "秋分");
        put("한로", "寒露"); put("상강", "霜降");
        put("입동", "立冬"); put("소설", "小雪"); put("대설", "大雪"); put("동지", "冬至");
        put("소한", "小寒"); put("대한", "大寒");
        // --- 중국에서만 쓰는 명절/기념일 (HIDE_CHINA_ONLY=true 이면 숨김) ---
        putChina("납팔절", "腊八", "腊八节", "臘八", "臘八節");
        putChina("소년절", "小年");
        putChina("용대두", "龙抬头", "龍抬頭");
        putChina("하원절", "下元节", "下元節");
        putChina("한의절", "寒衣节", "寒衣節");
        putChina("경로의 날", "老人节", "老人節");
        putChina("식목일", "植树节", "植樹節");
        putChina("청년절", "青年节", "青年節");
        putChina("어린이날", "儿童节", "兒童節");
        putChina("건당절", "建党节", "建黨節");
        putChina("건군절", "建军节", "建軍節");
        putChina("스승의 날", "教师节", "教師節");
        putChina("국경절", "国庆节", "國慶節", "国庆", "國慶");
        putChina("간호사의 날", "护士节", "護士節");
        putChina("기자의 날", "记者节", "記者節");
        putChina("열사 기념일", "烈士纪念日", "烈士紀念日");
        putChina("홍콩 반환 기념일", "香港回归纪念日", "香港回歸紀念日");
        putChina("마카오 반환 기념일", "澳门回归纪念日", "澳門回歸紀念日");
        // --- 한국에도 있는 날 / 국제 기념일 (한국어로 표시) ---
        put("신정", "元旦", "元旦节", "元旦節");
        put("밸런타인데이", "情人节", "情人節");
        put("세계 여성의 날", "妇女节", "婦女節");
        put("만우절", "愚人节", "愚人節");
        put("근로자의 날", "劳动节", "勞動節");
        put("어머니의 날", "母亲节", "母親節");
        put("아버지의 날", "父亲节", "父親節");
        put("핼러윈", "万圣节", "萬聖節");
        put("크리스마스 이브", "平安夜");
        put("크리스마스", "圣诞节", "聖誕節");
        put("추수감사절", "感恩节", "感恩節");
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
