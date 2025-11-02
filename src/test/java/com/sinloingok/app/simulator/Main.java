package com.sinloingok.app.simulator;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) {
//        String host = "47.94.103.164";
        String host = "127.0.0.1";

        int port = 18080;

        // 你要模拟的设备 onlyCode
        List<String> onlyCodes = Lists.newArrayList(
                "0090B2B6215D",
                "0090D500245B",
                "0090D5000F10",
                "0090D500242A",
                "0090D5000F4A",
                "0090B20057B9");

        Map<String, DeviceClient> map = new ConcurrentHashMap<>();
        for (String oc : onlyCodes) {
            new Thread(() -> {
                try {
                    DeviceClient c = new DeviceClient(host, port, oc);
                    if(!"0090B2B6215D".equals(oc)){
                        c.setPmDeviceClient(map.get("0090B2B6215D"));
                    }
                    map.put(oc, c);
                    c.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, oc).start();
        }

        // 控制台命令
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("命令:\n" +
                    "                  open  <onlyCode> <ch>       —— 打开单通道 (服务端兼容帧)\n" +
                    "                  close <onlyCode> <ch>       —— 关闭单通道 (服务端兼容帧)\n" +
                    "                  up    <onlyCode> <ch>       —— 上升沿上传 (文档OH=1)\n" +
                    "                  down  <onlyCode> <ch>       —— 下降沿上传 (文档OL=1)\n" +
                    "                  start <onlyCode> [times]    —— 随机轮番(1,2,4,7,8)，times省略=无限\n" +
                    "                  linear <onlyCode> [loops]  —— 线性测试 QS2→PM→XC→DM→XS→QS1\n" +
                    "                  stop  <onlyCode>            —— 停止当前测试（轮番/线性）\n" +
                    "                  list                        —— 查看连接与轮番状态\n" +
                    "                  quit                        —— 退出");
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] t = line.split("\\s+");
                String cmd = t[0].toLowerCase(Locale.ROOT);

                switch (cmd) {
                    case "quit" : System.exit(0);
                        break;
                    case "list" : map.forEach((k, v) ->
                            System.out.println(k + " -> " +
                                    ((v.channel() != null && v.channel().isActive()) ? "ACTIVE" : "INACTIVE")
                                    + ", round=" + v.isRoundRunning()
                                    + ", linear=" + v.isLinearRunning()));
                        break;
                    case "open" : {
                        if (t.length < 3) {
                            System.out.println("用法: open <onlyCode> <ch>");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        int ch = Integer.parseInt(t[2]);
                        c.sendOpenCompat(ch); // 兼容服务端 substring 的 begin/end
                        break;
                    }
                    case "close" : {
                        if (t.length < 3) {
                            System.out.println("用法: close <onlyCode> <ch>");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        int ch = Integer.parseInt(t[2]);
                        c.sendCloseCompat(ch);
                        break;
                    }
                    case "up" : {
                        if (t.length < 3) {
                            System.out.println("用法: up <onlyCode> <ch>");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        int ch = Integer.parseInt(t[2]);
                        c.sendDocRising(ch); // 文档版：OH=mask, OL=00
                        break;
                    }
                    case "down" : {
                        if (t.length < 3) {
                            System.out.println("用法: down <onlyCode> <ch>");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        int ch = Integer.parseInt(t[2]);
                        c.sendDocFalling(ch); // 文档版：OH=00, OL=mask
                        break;
                    }
                    case "start" : {
                        if (t.length < 2) {
                            System.out.println("用法: start <onlyCode> [times]");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        int times = (t.length >= 3) ? Integer.parseInt(t[2]) : 0;
                        c.startRound(times);
                        break;
                    }
                    case "linear" : {
                        if (t.length < 2) {
                            System.out.println("用法: linear <onlyCode> [loops]");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        int loops = (t.length >= 3) ? Integer.parseInt(t[2]) : 1;
                        c.startLinear(loops);
                        break;
                    }
                    case "stop" : {
                        if (t.length < 2) {
                            System.out.println("用法: stop <onlyCode>");
                            break;
                        }
                        DeviceClient c = map.get(t[1]);
                        if (c == null) {
                            System.out.println("未知 onlyCode");
                            break;
                        }
                        c.stopRound();
                        break;
                    }
                    default : System.out.println("未知命令: " + cmd);
                }
            }
        }
    }
}
