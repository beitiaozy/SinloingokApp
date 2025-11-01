package com.sinloingok.app.service;

import com.sinloingok.app.util.ns.SignalPayloadUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class NetSiteOrderDetailInsertGenerator {

    public static void main(String[] args) {

        System.out.println(SignalPayloadUtils.convert("01"));

        // 輸入 orderId
        long orderId = 15216;
        generateInsertSQL(orderId, 30);
    }

    public static void generateInsertSQL(long orderId, int count) {
        Random random = new Random();
        LocalDateTime current = LocalDateTime.now().plusSeconds(5); // 保證 begin_time > now
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < count; i++) {
            int duration = 10 + random.nextInt(11); // 區間 [5,20] 秒
            LocalDateTime beginTime = current;
            LocalDateTime endTime = beginTime.plusSeconds(duration);

            int type = random.nextBoolean() ? 1 : 2;

            String sql = String.format(
                "INSERT INTO net_site_order_detail (order_id, type, begin_time, end_time) " +
                "VALUES (%d, %d, '%s', '%s');",
                orderId, type, beginTime.format(formatter), endTime.format(formatter)
            );

            System.out.println(sql);

            // 更新 current，保證下一個 begin_time > 本次 end_time
            current = endTime.plusSeconds(1 + random.nextInt(5)); // 間隔 1~5 秒
        }
    }
}
