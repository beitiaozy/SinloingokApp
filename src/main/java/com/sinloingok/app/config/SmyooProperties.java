package com.sinloingok.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 思麓雲喇叭相關配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "smyoo")
public class SmyooProperties {

    /** 是否啟用雲喇叭播報。 */
    private boolean enabled = false;

    /** API 基礎地址，默認僅用於日誌顯示。 */
    private String baseUrl = "https://openapi.smyoo.com";

    /** 會話有效期，默認 12 小時。 */
    private Duration sessionTtl = Duration.ofHours(12);

    private final Common common = new Common();
    private final Auth auth = new Auth();
    private final Speaker speaker = new Speaker();
    private final Scheduler scheduler = new Scheduler();

    @Data
    public static class Common {
        private Integer uid;
        private String deviceId;
        private String clientId;
        private Integer protocol = 1;
    }

    @Data
    public static class Auth {
        private String phone;
        private String password;
        private String clientSecret;
    }

    @Data
    public static class Speaker {
        private String mcuid;
        private Integer datatype = 0;
        private String speed = "6";
        private String pitch = "6";
        private String volume = "10";
        private String person = "7";
    }

    @Data
    public static class Scheduler {
        private String cron = "0 0 4 * * *";
    }
}

