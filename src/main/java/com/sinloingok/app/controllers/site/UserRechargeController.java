package com.sinloingok.app.controllers.site;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.user.RechargeOrderResDto;
import com.sinloingok.app.dtos.user.RechargeRecordDto;
import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.netsite.BluetoothAddressRechargeService;
import com.sinloingok.app.service.netsite.BluetoothAddressService;
import com.sinloingok.app.service.user.RechargeOrderService;
import com.sinloingok.app.service.user.UserMoneyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 充值活动管理和充值明细查询
 */
@Slf4j
@RestController
@RequestMapping("/recharge")
public class UserRechargeController extends BaseController {

    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private BluetoothAddressService bluetoothAddressService;
    @Autowired
    private BluetoothAddressRechargeService rechargeService;
    @Autowired
    private UserMoneyRecordService userMoneyRecordService;

    /**
     * 获取充值码
     */
    @RequestMapping("getRechargeCode")
    @AuthCheck(LoginType.USER)
    @ResponseBody
    public StandardRtnDto<?> getRechargeCode(@RequestParam("rechargeId") long rechargeId) {
        User user = UserContext.getUser();
        // @TODO 下面這部分充值套餐的邏輯有待優化
        BluetoothAddressRecharge bluetoothAddressRecharge = rechargeService.findRechargeById(rechargeId);
        long orderId = rechargeOrderService.createChargeOrder(user, bluetoothAddressRecharge);
        return success(orderId);
    }

    /**
     * 充值活动列表
     */
    @RequestMapping("listRecharge")
    @AuthCheck(LoginType.USER)
    @ResponseBody
    public StandardRtnDto<?> listRecharge(@RequestBody Map<String, String> params) {
        long address_id = Long.valueOf(params.getOrDefault("address_id", "4"));
        User user = UserContext.getUser();
        try {
            List<BluetoothAddressRecharge> rechargs = rechargeService.rechargeOptionDtoList(user, address_id);
            return success(rechargs);
        } catch (Exception e) {
            log.error("充值活动列表系统错误", e);
            return error("充值活动列表系统错误");
        }
    }

    /**
     * 用户充值记录
     */
    @RequestMapping("rechargeRecord")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> rechargeRecord(PageData pageData) {
        User user = UserContext.getUser();
        SimplePageDto<RechargeRecordDto> page =
                userMoneyRecordService.pageRechargeRecord(user.getId(), pageData.getPageNo(), pageData.getPageSize());
        return success(page);
    }

    /**
     * 充值明细列表
     */
    @RequestMapping("listRechargeRecord")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> listRechargeRecord(@RequestBody PageData<RechargeRecordDto> pageData) {
        RechargeRecordDto recordDto = pageData.getBody();
        try {
            recordDto.setStatus("已充值");
            SimplePageDto<RechargeOrderResDto> record = rechargeOrderService.queryRechargeOrderResDto(pageData);

            Map<String, Object> result = rechargeOrderService.sumMoneyAwards(pageData.getBody());
            result.put("list", record);

            return success(result);
        } catch (Exception e) {
            log.error("充值活动列表系统错误", e);
        }
        return error("充值活动列表系统错误");
    }
}
