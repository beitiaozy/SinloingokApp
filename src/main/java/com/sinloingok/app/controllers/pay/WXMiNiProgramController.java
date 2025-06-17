package com.sinloingok.app.controllers.pay;

import com.alibaba.fastjson.JSON;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.service.netsite.BluetoothAddressService;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.pay.WXMiNiProgramService;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 微信小程序后台交互控制类
 */
@RestController
@RequestMapping("wXMiNiProgram")
@Slf4j
public class WXMiNiProgramController extends BaseController {

    @Autowired
    private WXMiNiProgramService programService;


    /**
     * 微信小程序访问授权
     */
    @RequestMapping("authorize")
    @ResponseBody
    public StandardRtnDto<?> authorize(@RequestBody Map<String, String> params) {
        return programService.authorize(params);
    }

    /**
     * 微信登陆/注册(Y)
     */
    @RequestMapping("signUpByWeiXin")
    @ResponseBody
    public StandardRtnDto<?> signUpByWeiXin(HttpServletRequest request, @RequestBody Map<String, String> params) {
        log.info(JSON.toJSONString(params));
        log.info(request.getParameter("open_id"));

        return programService.signUpByWeiXin(request, params);
    }

}
