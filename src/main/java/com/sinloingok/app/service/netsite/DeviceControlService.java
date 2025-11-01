package com.sinloingok.app.service.netsite;

import com.sinloingok.app.util.ns.NettyChannelRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceControlService {

    private final NettyChannelRegistry channelRegistry;

    /**
     * 臨時測試打開SLN05設備的1號電路
     */
    public void testPowerOnDevice(String onlyCode){
        // 1路继电器打开
        channelRegistry.sendMsg(onlyCode, "CCDDA10100010001A448");
    }

    /**
     * 臨時測試關閉SLN05設備的1號電路
     */
    public void testPowerOffDevice(String onlyCode){
        channelRegistry.sendMsg(onlyCode, "CCDDA10100000001A346");

    }

    /**
     * 臨時測試關閉SLN05設備的1號電路
     */
    public void testPowerReadDevice(String onlyCode){
        channelRegistry.sendMsg(onlyCode, "CCDDC00100000DCE9C");

    }
    public void testSendMsgToDevice(String onlyCode, String msg){
        channelRegistry.sendMsg(onlyCode, msg);

    }
}
