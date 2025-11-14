package com.sinloingok.app.constant;

/**
 * 洗淶樂 語音提示配置
 * - 每個場景提供：場景ID、分類、觸發條件、模板文本、可用變量
 * - 模板中變量使用 {nickname}/{project}/{balance}/{amount}/{eventName}/{weather}/{time}
 */
public final class VoicePrompts {

    private VoicePrompts() {}

    /** 變量名常量（用於格式化與校驗） */
    public static final class Vars {
        public static final String NICKNAME  = "nickname";   // 用戶暱稱（無則兜底為“用戶/微信用戶”）
        public static final String PROJECT   = "project";    // 當前啟動項目（清水/泡沫/鍍膜/吸塵…）
        public static final String BALANCE   = "balance";    // 當前餘額（元）
        public static final String AMOUNT    = "amount";     // 本次消費金額（元）
        public static final String EVENTNAME = "eventName";  // 當前活動名稱
        public static final String WEATHER   = "weather";    // 天氣（晴/雨/多雲…）
        public static final String TIME      = "time";       // 使用時間段（早上/下午/晚上）
        private Vars() {}
    }

    /** 語音場景（含：ID、分類、觸發、模板、變量列表） */
    public enum Scene {

        // A 進場 / 設備識別
        A01("A01","進場","設備待機 / 未掃碼","歡迎光臨洗淶樂，請掃碼開始洗車。",              new String[]{}),
        A02("A02","進場","車牌識別到車輛","車主您好，洗淶樂已為您準備好洗車服務。",              new String[]{}),
        A03("A03","進場","設備上線成功","設備已上線，隨時可用。",                                  new String[]{}),
        A04("A04","進場","設備維護中","抱歉，本設備正在維護中，請選擇其他機位。",                  new String[]{}),

        // B 啟動前
        B01("B01","啟動前","老用戶 / 無活動","歡迎回來，洗淶樂陪你一起清爽出行。",                new String[]{Vars.NICKNAME}),
        B02("B02","啟動前","老用戶 / 有活動","歡迎回來，充值優惠進行中，別錯過喔！",                new String[]{Vars.NICKNAME}),
        B03("B03","啟動前","有暱稱用戶","嗨，{nickname}，歡迎來洗淶樂！",                          new String[]{Vars.NICKNAME}),
        B04("B04","啟動前","新用戶首次掃碼","新朋友，歡迎來洗淶樂！你的專屬充值優惠已開啟。",        new String[]{}),
        B05("B05","啟動前","餘額不足提示","{nickname}，你的餘額快不夠啦，記得充值喔～",              new String[]{Vars.NICKNAME}),

        // C 使用中
        C01("C01","使用中","模式啟動","{project} 已啟動。",                                          new String[]{Vars.PROJECT}),
        C02("C02","使用中","模式暫停","{project} 已暫停。",                                          new String[]{Vars.PROJECT}),
        C03("C03","使用中","洗手暫停（推薦活動）","洗手休息一下？順便看看月卡優惠喔～",               new String[]{}),
        C04("C04","使用中","使用時長提醒（5-10分鐘）","{nickname}，時間過得真快～洗車快完成啦？",      new String[]{Vars.NICKNAME}),
        C05("C05","使用中","高壓/水壓異常","檢測到暫停，請確認噴槍狀態。",                           new String[]{}),
        C06("C06","使用中","餘額即將不足","餘額不足，請儘快充值以免中斷服務。",                      new String[]{Vars.BALANCE}),
        C07("C07","使用中","用戶多次使用","{nickname}，感謝你常來～記得留下建議喔！",                 new String[]{Vars.NICKNAME}),

        // D 結束/結算
        D01("D01","結束","用戶點擊關機","訂單已結算，共消費 {amount} 元。感謝使用洗淶樂，期待再見！", new String[]{Vars.AMOUNT}),
        D02("D02","結束","系統自動關單","系統已自動結束訂單，感謝使用洗淶樂。",                      new String[]{}),
        D03("D03","結束","暱稱設置引導","想更個性？打開小程序點頭像，設你的專屬暱稱～",               new String[]{}),

        // E 支付/充值
        E01("E01","支付","充值成功","充值成功，餘額 {balance} 元，感謝支持洗淶樂！",                   new String[]{Vars.BALANCE}),
        E02("E02","支付","支付異常","支付出現異常，請稍後重試或聯繫客服。",                           new String[]{}),
        E03("E03","支付","使用優惠券成功","恭喜！已使用優惠券，更划算地洗車～",                        new String[]{}),

        // F 推廣/運營
        F01("F01","推廣","活動推送 / 節日優惠","洗淶樂感恩季，充值送豪禮，點擊查看詳情！",             new String[]{Vars.EVENTNAME}),
        F02("F02","推廣","天氣提示（晴天）","今天天氣真好，來場清爽洗車吧！",                          new String[]{Vars.WEATHER}),
        F03("F03","推廣","天氣提示（下雨）","下雨天洗車也能享優惠喔～",                                new String[]{Vars.WEATHER}),
        F04("F04","推廣","會員週期滿週/月","洗淶樂陪你一年啦～感謝支持！",                             new String[]{Vars.NICKNAME}),
        F05("F05","推廣","常用時段建議","這個時段人少，洗車更方便喔～",                                new String[]{Vars.TIME}),

        // G 系統/安全
        G01("G01","系統","控制器離線","設備暫時無法使用，請稍後再試。",                                new String[]{}),
        G02("G02","系統","網絡中斷","網絡異常，請檢查連線或聯繫客服。",                                new String[]{}),
        G03("G03","系統","水壓過低","水壓偏低，請稍候再試。",                                         new String[]{}),
        G04("G04","系統","安全警示","請注意安全，避免噴槍指向人體。",                                  new String[]{});

        private final String id;
        private final String category;
        private final String trigger;
        private final String template;
        private final String[] variables;

        Scene(String id, String category, String trigger, String template, String[] variables) {
            this.id = id;
            this.category = category;
            this.trigger = trigger;
            this.template = template;
            this.variables = variables;
        }

        public String getId() { return id; }
        public String getCategory() { return category; }
        public String getTrigger() { return trigger; }
        public String getTemplate() { return template; }
        public String[] getVariables() { return variables; }
    }
}
