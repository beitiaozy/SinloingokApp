package com.sinloingok.app.util.ns;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DeviceControlService2 {
    public static String COMMAND_OPEN = "OPEN";
    public static String COMMAND_CLOSE = "CLOSE";

    public static String COMMAND_READ = "READ";


    public void executorCommand(String onlyCode, int channel, String command) {
        String commandKey = command + "-" + channel;
        String msg = COMMAND_MAP.get(commandKey);
        if (StringUtils.isNotEmpty(msg)) {
            HandlerServer.sendMsg(onlyCode, msg);
        } else {
            log.info("{}执行的{}命令不存在", onlyCode, command);
        }
    }


    private static final Map<String, String> COMMAND_MAP = new HashMap<String, String>() {{
        put("OPEN-1", "CCDDA10100010001A448");
        put("CLOSE-1", "CCDDA10100000001A346");
        put("OPEN-2", "CCDDA10100020002A64C");
        put("CLOSE-2", "CCDDA10100000002A448");
        put("OPEN-3", "CCDDA10100040004AA54");
        put("CLOSE-3", "CCDDA10100000004A64C");
        put("OPEN-4", "CCDDA10100080008B264");
        put("CLOSE-4", "CCDDA10100000008AA54");
        put("OPEN-0", "CCDDA101FFFFFFFF9E3C");
        put("CLOSE-0", "CCDDA1010000FFFFA040");
        put("READ-0", "CCDDC00100000DCE9C");
    }};
}
