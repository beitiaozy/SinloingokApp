package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.BluetoothAddressResDto;
import com.sinloingok.app.dtos.NetSiteStatsResDto;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.user.UserMoneyStatsGroupedByPeriod;
import com.sinloingok.app.kits.MD5;
import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.service.netsite.BluetoothAddressService;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.ManageUserService;
import com.sinloingok.app.service.user.UserMoneyRecordService;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.NextCodeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 蓝牙相关接口
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@RestController
@RequestMapping("manage")
public class ManageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ManageController.class);

    @Autowired
    private NetSiteService netSiteService;
    @Autowired
    private ManageUserService manageUserService;
    @Autowired
    private UserMoneyRecordService userMoneyRecordService;

    @Autowired
    private BluetoothAddressService bluetoothAddressService;


    /**
     * 代理商/操作员登录
     */
    @RequestMapping("login")
    public StandardRtnDto<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (ObjectUtils.anyNull(username, password)) {
            return error("参数为空");
        }

        try {
            ManageUser u = manageUserService.findByMobile(username);

            if (u == null) {
                return error("手机号不存在");
            }

            ManageUser user = manageUserService.findByMobileAndPassword(username, MD5.md5(password, "utf-8"));

            if (user == null) {
                return error("密码错误");
            }

            if (user.getStatus().equals(SinloingokUserStatus.ManageUserStatus.NO_USING)) {
                return error("账号被禁用");
            }

            user.setLastLoginTime(DateUtils.curTime());
            user.setRandomCode(NextCodeUtils.nextRandomCode());
            manageUserService.update(user);

            Map<String, Object> result = new HashMap<String, Object>();

            if (user.getRoleId() == SinloingokUserStatus.RoleID.DAILI) {
                result.put("type", "代理商");
            } else if (user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU) {
                result.put("type", "商户");
            }

            result.put("username", user.getUsername());
            result.put("mobile", user.getMobile());
            result.put("rate", user.getRate());
            result.put("token", NextCodeUtils.getToken(user.getId(), user.getRandomCode()));
            result.put("user_id", user.getId());
            result.put("daili_rate", user.getDailiRate());

            return success(result);
        } catch (Exception e) {
            logger.error("代理商操作员登录系统错误", e);
            return error("代理商操作员登录系统错误");
        }
    }

    /**
     * 首页信息
     */
    @RequestMapping("homeInfo")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> homeInfo() {
        try {
            Map<String, Object> result = new HashMap<>();
            UserMoneyStatsGroupedByPeriod period = userMoneyRecordService.selectGroupedMoneyStats();
            NetSiteStatsResDto resDto = netSiteService.selectNetSiteStats();
            result.put("resDto", resDto);
            result.put("period", period);
            return success(result);
        } catch (Exception e) {
            logger.error("首页信息系统错误", e);
            return error("首页信息系统错误");
        }
    }

    /**
     * 修改密码
     */
    @RequestMapping("updatePsd")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<String> updatePsd(@RequestBody Map<String, String> params) {
        String old_psd = params.get("old_psd");
        String new_psd = params.get("new_psd");
        ManageUser user = UserContext.getMUser();
        if (user == null) {
            return error("代理商需要登录！");
        }

        if (user.getStatus().equals(SinloingokUserStatus.ManageUserStatus.NO_USING)) {
            return error("账号异常！");
        }

        if (ObjectUtils.anyNull(old_psd, new_psd)) {
            return error("参数为空");
        }

        try {
            if (!user.getPassword().equals(MD5.md5(old_psd, "utf-8"))) {
                return error("原密码错误");
            }

            user.setPassword(MD5.md5(new_psd, "utf-8"));
            user.setRandomCode(NextCodeUtils.nextRandomCode());
            manageUserService.update(user);

            return success("修改密码成功 请重新登录");
        } catch (Exception e) {
            logger.error("修改密码系统错误", e);
            return error("修改密码系统错误");
        }
    }

    @RequestMapping("queryAddressList")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<List<BluetoothAddressResDto>> queryBluetoothAddressList() {
        return success(bluetoothAddressService.queryBluetoothAddressList());
    }
}
