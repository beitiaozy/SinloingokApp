package com.sinloingok.app.dtos.user;

public class UserMoneyRecordStatDto {

    private String type;             // 余额变动类型
    private Long countTotal;         // 总笔数

    private Double todayMoney;       // 今日金额
    private Long todayCount;         // 今日笔数

    private Double yesterdayMoney;   // 昨日金额
    private Long yesterdayCount;     // 昨日笔数

    private Double monthMoney;       // 本月金额
    private Long monthCount;         // 本月笔数

    // --- Getters & Setters ---

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(Long countTotal) {
        this.countTotal = countTotal;
    }

    public Double getTodayMoney() {
        return todayMoney;
    }

    public void setTodayMoney(Double todayMoney) {
        this.todayMoney = todayMoney;
    }

    public Long getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(Long todayCount) {
        this.todayCount = todayCount;
    }

    public Double getYesterdayMoney() {
        return yesterdayMoney;
    }

    public void setYesterdayMoney(Double yesterdayMoney) {
        this.yesterdayMoney = yesterdayMoney;
    }

    public Long getYesterdayCount() {
        return yesterdayCount;
    }

    public void setYesterdayCount(Long yesterdayCount) {
        this.yesterdayCount = yesterdayCount;
    }

    public Double getMonthMoney() {
        return monthMoney;
    }

    public void setMonthMoney(Double monthMoney) {
        this.monthMoney = monthMoney;
    }

    public Long getMonthCount() {
        return monthCount;
    }

    public void setMonthCount(Long monthCount) {
        this.monthCount = monthCount;
    }
}
