package com.sinloingok.app.service.pay;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.dao.UserAddressAccountDao;
import com.sinloingok.app.dao.status.UserStatus;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.kits.Base64;
import com.sinloingok.app.kits.MD5;
import com.sinloingok.app.models.fund.UserAddressAccount;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.netsite.BluetoothAddressService;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.NetUtils;
import com.sinloingok.app.util.NextCodeUtils;
import com.sinloingok.app.util.PropertiesUtils;
import com.sinloingok.app.util.pay.WXPayUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WXMiNiProgramService{
    private static final Logger logger = LoggerFactory.getLogger(WXMiNiProgramService.class);
    private final static String AppID = PropertiesUtils.getProperties().getProperty("WEIXIN_APPID");
    private final static String AppSecret = PropertiesUtils.getProperties().getProperty("WEIXIN_APPSECRET");

    @Autowired
    private UserService userService;
    @Autowired
    private NetSiteService netSiteService;
    @Autowired
    private BluetoothAddressService bluetoothAddressService;

    @Autowired
    private UserAddressAccountDao accountDao;

    public StandardRtnDto<?> authorize(Map<String, String> params) {
        String code = params.get("code");
        String encrypted = params.get("encrypted");
        String iv = params.get("iv");
        long addressId = Long.valueOf(params.getOrDefault("dev_id", String.valueOf(SysConstant.DEFAULT_ADDRESS)));

        if (StringUtils.isEmpty(code)) {
            return StandardRtnDto.error("临时登录凭证 code不能为空");
        }

        try {
            JSONObject sessionInfo = getWeiXinSessionInfo(code);
            String openId = sessionInfo.getString("openid");

            if (openId != null) {
                String accessToken = getAccessToken();
                User user = userService.findByOpenId(openId, addressId);

                if (user == null) {
                    return handleNewUser(encrypted, iv, sessionInfo, openId);
                } else {
                    return updateUserMessage(user, openId, accessToken);
                }
            } else {
                return StandardRtnDto.error("授权失败");
            }
        } catch (Exception e) {
            logger.error("授权失败", e);
            return StandardRtnDto.error("授权失败");
        }
    }

    public StandardRtnDto<?> signUpByWeiXin(HttpServletRequest request, Map<String, String> params) {
        // 参数校验
        if (StringUtils.isEmpty(params.get("open_id"))) {
            return StandardRtnDto.error("参数为空");
        }
//        if (StringUtils.isEmpty(params.get("dev_id"))) {
//            return StandardRtnDto.error("请先扫码再授权");
//        }

        try {
            String accessToken = getAccessToken();
            User user = userService.findByOpenId(params.get("open_id"), SysConstant.DEFAULT_ADDRESS);

            if (user != null) {
                return handleExistingUser(request, params, user, accessToken);
            } else {
                return handleNewRegistration(request, params, accessToken);
            }
        } catch (Exception e) {
            logger.error("注册失败", e);
            return StandardRtnDto.error("注册失败");
        }
    }

    public StandardRtnDto<?> updateUserMessage(User user, String openId, String accessToken) {
        Map<String, Object> result = new HashMap<>();
        user.setRandomCode(NextCodeUtils.nextRandomCode());
        user.setLastLoginIp(NetUtils.getRequestIP(
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()));
        user.setLastLoginTime(DateUtils.curTime());
        user.setOpenId(openId);
        userService.update(user);

        result.put("token", NextCodeUtils.getToken(user.getId(), user.getRandomCode()));
        result.put("user_id", user.getId());
        result.put("role_id", user.getRoleId());
        result.put("open_id", user.getOpenId());
        result.put("access_token", accessToken);
        return StandardRtnDto.success(result);
    }

    public String getAccessToken() throws Exception {
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, String> params = new HashMap<>();
        params.put("appid", AppID);
        params.put("secret", AppSecret);
        params.put("grant_type", "client_credential");
        JSONObject res = JSONObject.parseObject(NetUtils.http_get(url, params));
        return res.getString("access_token");
    }

    // ============= 私有方法 =============

    private JSONObject getWeiXinSessionInfo(String code) throws Exception {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, String> params = new HashMap<>();
        params.put("appid", AppID);
        params.put("secret", AppSecret);
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");
        return JSONObject.parseObject(NetUtils.http_get(url, params));
    }

    private StandardRtnDto<?> handleNewUser(String encrypted, String iv, JSONObject sessionInfo, String openId) throws Exception {
        if (StringUtils.isNotEmpty(encrypted) && StringUtils.isNotEmpty(iv)) {
            String result = WXPayUtil.wxDecrypt(encrypted, sessionInfo.getString("session_key"), iv);
            JSONObject jo = JSONObject.parseObject(result);
            String mobile = jo.getString("phoneNumber");
            User user = userService.findByOpenId(openId, SysConstant.DEFAULT_ADDRESS);
            if (mobile != null && user != null) {
                return updateUserMessage(user, openId, getAccessToken());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("openid", openId);
            map.put("mobile", mobile);
            return StandardRtnDto.success(map);
        } else {
            return StandardRtnDto.success(sessionInfo.get("openid"));
        }
    }

    private StandardRtnDto<?> handleExistingUser(HttpServletRequest request, Map<String, String> params, User user, String accessToken) throws Exception {
        // 验证逻辑
        if (user.getId().longValue() == Long.valueOf(params.getOrDefault("invite_mobile", "-1")).longValue()) {
            return StandardRtnDto.error("不可以邀请自己");
        }

        // 处理手机号
        String mobile = params.get("mobile");
        if (StringUtils.isNotEmpty(params.get("mobile_code"))) {
            mobile = getPhoneNumber(accessToken, params.get("mobile_code"));
        }

        // 更新用户信息
        user.setUsername(mobile);
        if (StringUtils.isNotEmpty(mobile)) {
            user.setMobile(mobile);
        }
        user.setAvatar(params.get("headimgurl"));
        user.setNickname(Base64.encode(params.get("nickname")));
        user.setRandomCode(NextCodeUtils.nextRandomCode());
        user.setLastLoginIp(NetUtils.getRequestIP(request));
        user.setLastLoginTime(DateUtils.curTime());
        userService.update(user);

        // 构建返回结果
        return buildUserResult(user, accessToken);
    }

    private StandardRtnDto<?> handleNewRegistration(HttpServletRequest request, Map<String, String> params, String accessToken) throws Exception {
        // 处理手机号
        String mobile = params.get("mobile");
        if (StringUtils.isNotEmpty(params.get("mobile_code"))) {
            mobile = getPhoneNumber(accessToken, params.get("mobile_code"));
        }
        logger.info("===================测试注册用户的mobile为：", mobile);
        // 创建新用户
        User user = new User();
        user.setOpenId(params.get("open_id"));
        user.setAvatar(params.get("headimgurl"));
        user.setNickname(Base64.encode(params.get("nickname")));
        user.setPassword(MD5.md5(mobile, "utf-8"));
        user.setRoleId(62L);
        user.setRandomCode(NextCodeUtils.nextRandomCode());
        user.setCreateTime(DateUtils.curTime());
        user.setUsername(mobile);
        user.setMobile(mobile);
        user.setLevel(SinloingokUserStatus.UserLevel.NORMAL);
        user.setStatus(UserStatus.NORMAL);
        user.setExtMoney(0.00f);
        user.setBalance(0.00f);
        user.setLastLoginIp(NetUtils.getRequestIP(request));
        user.setLastLoginTime(DateUtils.curTime());
        userService.insert(user);
        // 处理设备信息
        String devId = params.get("dev_id");
        if (StringUtils.isEmpty(devId)) {
           devId = "0090D5000F4A";
        }
        NetSite netSite = netSiteService.selectByOnlyCode(devId);
        Integer newRechargeDay = bluetoothAddressService.findById(netSite.getAddressId())
                .getNewRechargeDay();
        user.setNewRechargeTime(
                new DateUtils(DateUtils.FORMAT).add(DateUtils.DAY, newRechargeDay).toString());
        user.setDevId(devId);
        UserAddressAccount userAddressAccount = new UserAddressAccount(user.getId(), netSite.getAddressId(), new BigDecimal(user.getBalance()), new BigDecimal(user.getExtMoney()));
        user.setCurrentAccount(userAddressAccount);
        userService.update(user);
        return buildUserResult(user, accessToken);
    }

    private String getPhoneNumber(String accessToken, String mobileCode) throws Exception {
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
        JSONObject jb = new JSONObject();
        jb.put("code", mobileCode);
        String result = NetUtils.http_post(url, new HashMap<>(), jb.toJSONString());
        return JSONObject.parseObject(result).getJSONObject("phone_info").getString("phoneNumber");
    }

    private StandardRtnDto<?> buildUserResult(User user, String accessToken) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", NextCodeUtils.getToken(user.getId(), user.getRandomCode()));
        map.put("user_id", user.getId());
        map.put("money", user.getMoney());
        map.put("mobile", user.getMobile());
        map.put("avatar", user.getAvatar());
        map.put("nickname", Base64.decode(user.getNickname()));
        map.put("open_id", user.getOpenId());
        map.put("role_id", user.getRoleId());
        map.put("access_token", accessToken);
        map.put("level", user.getLevel());
        return StandardRtnDto.success(map);
    }

}