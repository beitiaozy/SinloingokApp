package com.sinloingok.app.service.netsite;

import com.alibaba.fastjson.JSON;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.util.ns.HandlerServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeviceControlService {

    @Autowired
    private NetSiteService netSiteService;




    /**
     * 臨時測試打開SLN05設備的1號電路
     */
    public void testPowerOnDevice(String onlyCode){
        // 1路继电器打开
        HandlerServer.sendMsg(onlyCode, "CCDDA10100010001A448");
    }

    /**
     * 臨時測試關閉SLN05設備的1號電路
     */
    public void testPowerOffDevice(String onlyCode){
        HandlerServer.sendMsg(onlyCode, "CCDDA10100000001A346");

    }

    /**
     * 臨時測試關閉SLN05設備的1號電路
     */
    public void testPowerReadDevice(String onlyCode){
        HandlerServer.sendMsg(onlyCode, "CCDDC00100000DCE9C");

    }
    public void testSendMsgToDevice(String onlyCode, String msg){
        HandlerServer.sendMsg(onlyCode, msg);

    }
}
