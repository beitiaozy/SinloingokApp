package com.sinloingok.app.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * 作用：对一个阳历日期做 set、get 操作以及前进后退操作（基于 java.time API 实现）
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
public class DateUtils {

    /**
     * 日期中年份的数值
     */
    public static final int YEAR = 0x01;
    /**
     * 一年的第几个月
     */
    public static final int MONTH = 0x02;
    /**
     * 一个月中的第多少天
     */
    public static final int DAY = 0x03;
    /**
     * 小时
     */
    public static final int HOUR = 0x04;
    /**
     * 分钟
     */
    public static final int MINUTE = 0x05;
    /**
     * 秒
     */
    public static final int SECOND = 0x06;
    /**
     * 一周中的星期几，约定：星期日为 0、星期一为 1，以此类推
     */
    public static final int WEEK = 0x07;
    /**
     * 一年中第几天
     */
    public static final int DAY_IN_YEAR = 0x08;
    /**
     * 一个月中的第几周
     */
    public static final int WEEK_IN_MONTH = 0x09;
    /**
     * 一年中的第几周
     */
    public static final int WEEK_IN_YEAR = 0x0A;

    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 默认日期格式
    // 使用 LocalDateTime 存储日期时间
    private LocalDateTime dateTime;
    private DateTimeFormatter formatter;

    public static String curTime() {
        return new DateUtils(System.currentTimeMillis()).toString(FORMAT);
    }

    public static String todayDate(int num) {
        LocalDate today = LocalDate.now().plusDays(num);
        return today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String curTimeAddSeconds(int seconds){
        return new DateUtils(System.currentTimeMillis()).addSeconds(seconds).toString(FORMAT);
    }

    public static String actualTimeAddSeconds(String actualTime, int seconds){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT);
        return new DateUtils(LocalDateTime.parse(actualTime, formatter)).addSeconds(seconds).toString(FORMAT);
    }

    // 默认构造：当前时间
    public DateUtils() {
        this(FORMAT);
    }


    // 构造时指定格式（当前时间）
    public DateUtils(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
        this.dateTime = LocalDateTime.now();
    }

    // 构造时以 LocalDateTime 赋值
    public DateUtils(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.formatter = DateTimeFormatter.ofPattern(FORMAT);
    }

    // 以毫秒构造对象
    public DateUtils(long milliseconds) {
        this.dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        this.formatter = DateTimeFormatter.ofPattern(FORMAT);
    }

    // 根据指定格式解析日期字符串构造对象
    public DateUtils(String datePattern, String dateStr) {
        this.formatter = DateTimeFormatter.ofPattern(datePattern);
        this.dateTime = LocalDateTime.parse(dateStr, this.formatter);
    }

    public DateUtils addSeconds(int seconds) {
        this.dateTime = this.dateTime.plusSeconds(seconds);
        return this;
    }

    /**
     * 返回字符串形式的日期值，格式为构造时设置的格式
     */
    public String toString() {
        return dateTime.format(formatter);
    }

    /**
     * 返回指定格式的日期字符串
     *
     * @param datePattern 希望的日期格式
     * @return 格式化后的字符串
     */
    public String toString(String datePattern) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(datePattern);
        return dateTime.format(fmt);
    }

    /**
     * 返回当前时间对应的毫秒数
     */
    public long getMilliseconds() {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }


    /**
     * 将当前时间更新为传入的 LocalDateTime
     */
    public DateUtils setDate(LocalDateTime dt) {
        this.dateTime = dt;
        return this;
    }

    /**
     * 以毫秒更新当前时间
     */
    public DateUtils setDate(long milliseconds) {
        this.dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        return this;
    }


    /**
     * 在指定字段上增加 n（正数表示增加，单位为年、月、周、日、小时、分钟、秒）
     */
    public DateUtils add(int datefield, long n) {
        switch (datefield) {
            case YEAR:
                dateTime = dateTime.plusYears(n);
                break;
            case MONTH:
                dateTime = dateTime.plusMonths(n);
                break;
            case WEEK:
                dateTime = dateTime.plusWeeks(n);
                break;
            case DAY:
                dateTime = dateTime.plusDays(n);
                break;
            case HOUR:
                dateTime = dateTime.plusHours(n);
                break;
            case MINUTE:
                dateTime = dateTime.plusMinutes(n);
                break;
            case SECOND:
                dateTime = dateTime.plusSeconds(n);
                break;
            default:
                System.err.println("Invalid field for add: " + datefield);
        }
        return this;
    }

    /**
     * 将当前时间更新为系统当前时间
     */
    public DateUtils update() {
        dateTime = LocalDateTime.now();
        return this;
    }

    /**
     * 计算两个 LocalDate 之间的天数差（整数）
     */
    public static int daysBetween(LocalDate d1, LocalDate d2) {
        return (int) ChronoUnit.DAYS.between(d1, d2);
    }

    /**
     * 计算两个 DateTime 对象之间相差的天数（按日期计算）
     */
    public static int daysBetween(DateUtils dt1, DateUtils dt2) {
        return (int) ChronoUnit.DAYS.between(dt1.dateTime.toLocalDate(), dt2.dateTime.toLocalDate());
    }

    /**
     * 根据字符串形式的日期（格式："yyyy-MM-dd HH:mm:ss"）计算两天之间的天数差
     */
    public static int daysBetween(String smdate, String bdate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt1 = LocalDateTime.parse(smdate, fmt);
        LocalDateTime dt2 = LocalDateTime.parse(bdate, fmt);
        return (int) ChronoUnit.DAYS.between(dt1.toLocalDate(), dt2.toLocalDate());
    }

    /**
     *  计算方法: bdate - smdate;
     * @param smdate
     * @param bdate
     * @return
     */
    public static int daysBetween2(String smdate, String bdate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dt1 = LocalDate.parse(smdate, fmt);
        LocalDate dt2 = LocalDate.parse(bdate, fmt);
        return (int) ChronoUnit.DAYS.between(dt1, dt2);
    }

    /**
     * 判断当前时间与指定时间之间是否相差超过 2 小时。
     * 注意：返回 true 表示 cueTime 比 time 超前超过 2 小时。
     */
    public static boolean hoursDifference(String cueTime, String time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt1 = LocalDateTime.parse(cueTime, fmt);
        LocalDateTime dt2 = LocalDateTime.parse(time, fmt);
        long diff = ChronoUnit.HOURS.between(dt2, dt1);
        return diff > 2;
    }

    /**
     * 返回两个时间字符串之间相差的秒数。
     *
     * @param endTime 时间1，例如 "2025-06-16 12:00:00"
     * @param startTime    时间2，例如 "2025-06-16 11:58:30"
     * @return 相差的秒数（时间1 - 时间2）
     */
    public static long secondsDifference(String endTime, String startTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt1 = LocalDateTime.parse(endTime, fmt);
        LocalDateTime dt2 = LocalDateTime.parse(startTime, fmt);
        return ChronoUnit.SECONDS.between(dt2, dt1); // 注意顺序：dt1 - dt2
    }


    /**
     * 获取当前日所在星期范围，返回上周最后一天和本周最后一天的字符串表示。
     * 这里假设一周从周一开始，周日为最后一天。
     */
    public static Map<String, String> getLastDayOfWeek() {
        LocalDate today = LocalDate.now();
        // 默认周一作为一周的第一天
        WeekFields wf = WeekFields.of(Locale.getDefault());
        // 获取本周的周日
        LocalDate sunday = today.with(wf.dayOfWeek(), 7);
        // 上一周的同一天
        LocalDate lastWeekSunday = sunday.minusWeeks(1);

        String time1 = lastWeekSunday.toString() + " 23:59:59";
        String time2 = sunday.toString() + " 23:59:59";

        Map<String, String> result = new HashMap<>();
        result.put("time1", time1);
        result.put("time2", time2);
        return result;
    }

    public static String yesterdayFormated() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * main 方法仅用于简单测试
     */
    public static void main(String[] args) throws Exception {
        int a = daysBetween("2019-11-01 18:05:31", "2019-11-02 18:05:31");
        System.out.println("天数差：" + a);
        int a2 = daysBetween2("2099-11-01", "2019-11-02");
        System.out.println("天数差：" + a2);

        // 示例：使用 add 方法
        DateUtils dt = new DateUtils("yyyy/MM/dd HH:mm:ss", "2025/06/15 14:30:00");
        System.out.println("原始时间：" + dt);
        dt.add(SECOND, 10);
        System.out.println("加 10 秒后：" + dt);
    }


    private static final Set<String> generatedNumbers = new HashSet<>();
    private static final Random random = new Random();

    /**
     * 生成不重复的 8 位随机数字
     *
     * @return 不重复的 8 位随机数字
     */
    public static String generateUniqueRandomNumber() {
        String randomNumber;
        // 确保生成的数字是 8 位
        do {
            randomNumber = String.format("%08d", random.nextInt(100000000));
        } while (generatedNumbers.contains(randomNumber)); // 如果数字重复，重新生成
        generatedNumbers.add(randomNumber); // 添加到已生成集合中
        return randomNumber;
    }

}
