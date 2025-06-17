package com.sinloingok.app.dtos.user;

import lombok.Data;

@Data
public class UserMoneyStatsGroupedByPeriod {

    private PeriodStat today;
    private PeriodStat yesterday;
    private PeriodStat month;
    @Data
    public static class PeriodStat {
        private Double rechargeMoney = 0.0;
        private Long rechargeCount = 0L;
        private Double consumeMoney = 0.0;
        private Long consumeCount = 0L;
    }
}
