package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import com.sinloingok.app.service.netsite.BluetoothAddressRechargeService;
import com.sinloingok.app.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 场地充值管理(管理端维护和客户端查询)
 *
 * @author Jun
 */
@Slf4j
@RestController
@RequestMapping("manageRecharge")
public class ManageRechargeController extends BaseController {

    @Autowired
    private BluetoothAddressRechargeService bluetoothAddressService;

    /**
     * 添加充值活动
     */
    @RequestMapping("addRecharge")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> addRecharge(@RequestBody BluetoothAddressRecharge recharge) {
        try {
            bluetoothAddressService.insertRecharge(recharge);

            return success("添加充值活动成功");
        } catch (Exception e) {
            log.error("添加充值活动系统错误", e);
        }
        return error("添加充值活动系统错误");
    }

    /**
     * 删除充值活动
     */
    @RequestMapping("delRecharge")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> delRecharge(@RequestBody Map<String, String> params) {
        long recharge_id = Long.valueOf(params.getOrDefault("recharge_id", "-1"));
        try {
            bluetoothAddressService.deleteRecharge(recharge_id);
            return success("删除充值活动成功");
        } catch (Exception e) {
            log.error("删除充值活动系统错误", e);
        }
        return error("删除充值活动系统错误");
    }

    /**
     * 修改充值活动
     */
    @RequestMapping("editRecharge")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> editRecharge(@RequestBody BluetoothAddressRecharge recharge) {
        try {
            recharge.setCreateTime(DateUtils.curTime());
            bluetoothAddressService.updateRecharge(recharge);
            return success("修改充值活动成功");
        } catch (Exception e) {
            log.error("修改充值活动系统错误", e);
        }
        return error("修改充值活动系统错误");
    }

    /**
     * 修改充值活动
     */
    @RequestMapping("updateRechargeStatus")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> updateRechargeStatus(@RequestBody BluetoothAddressRecharge recharge) {
        try {
            recharge.setCreateTime(DateUtils.curTime());
            bluetoothAddressService.updateRecharge(recharge);
            return success("修改充值活动成功");
        } catch (Exception e) {
            log.error("修改充值活动系统错误", e);
        }
        return error("修改充值活动系统错误");
    }


    /**
     * 展示自己已经选择过的充值活动列表
     */
    @RequestMapping("myRechargeList")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> myRechargeList(@RequestBody Map<String, String> params) {
        long address_id = Long.valueOf(params.getOrDefault("address_id", "-1"));
        return success(bluetoothAddressService.rechargeList(address_id));
    }

    @RequestMapping("genericRechargeMapping")
    public Map<String, String> genericRechargeMapping(){
        return GenericRecharge.getAllGenreMapping();
    }
}
