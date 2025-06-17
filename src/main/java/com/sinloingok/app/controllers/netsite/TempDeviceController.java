package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dao.status.BluetoothAddressStatus;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.address.AddressResDto;
import com.sinloingok.app.models.bluetooth.BluetoothAddress;
import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.service.netsite.BluetoothAddressService;
import com.sinloingok.app.service.netsite.DeviceControlService;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.PropertiesUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 *  測試設備控製類
 *
 * @author Jun
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("testDeviceControl")
public class TempDeviceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TempDeviceController.class);

    @Autowired
    private DeviceControlService deviceControlService;
    /**
     * 员工查看场所列表
     */
    @PostMapping("testPowerOnDevice")
    public String testPowerOnDevice(@RequestBody Map<String, String> params) {
        String onlyCode = params.containsKey("onlyCode") ? params.get("onlyCode") : "0090B2B6215D";
        deviceControlService.testPowerOnDevice(onlyCode);
        return "SUCCESS";
    }

    @PostMapping("testPowerOffDevice")
    public void testPowerOffDevice(@RequestBody Map<String, String> params) {
        String onlyCode = params.containsKey("onlyCode") ? params.get("onlyCode") : "0090B2B6215D";
        deviceControlService.testPowerOffDevice(onlyCode);
    }

    @PostMapping("testPowerReadDevice")
    public void testPowerReadDevice(@RequestBody Map<String, String> params) {
        String onlyCode = params.containsKey("onlyCode") ? params.get("onlyCode") : "0090B2B6215D";
        deviceControlService.testPowerReadDevice(onlyCode);
    }



    @PostMapping("testSendMsgToDevice")
    public void testSendMsgToDevice(@RequestBody Map<String, String> params) {
        String onlyCode = params.containsKey("onlyCode") ? params.get("onlyCode") : "0090B2B6215D";
        String msg = params.containsKey("msg") ? params.get("msg") : "0090B2B6215D";
        deviceControlService.testSendMsgToDevice(onlyCode, msg);
    }
}
