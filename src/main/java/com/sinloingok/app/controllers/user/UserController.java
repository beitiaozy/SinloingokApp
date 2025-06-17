package com.sinloingok.app.controllers.user;

import com.sinloingok.app.aop.*;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.kits.Base64;
import com.sinloingok.app.models.activity.ActivityIntro;
import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.activity.ActivityIntroService;
import com.sinloingok.app.service.notice.PlatformNoticeService;
import com.sinloingok.app.service.user.ManageUserService;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.util.NextCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    protected UserService userService;
    @Autowired
    private ManageUserService manageUserService;
    @Autowired
    private ActivityIntroService activityIntroService;
    @Autowired
    private PlatformNoticeService platformNoticeService;


    /**
     * 个人信息接口(Y)
     */
    @RequestMapping("personInfo")
    @AuthCheck(LoginType.USER)
    @ResponseBody
    public StandardRtnDto<?> personInfo() {
        User user = UserContext.getUser();
        Map<String, Object> map = new HashMap<>();
        map.put("token", NextCodeUtils.getToken(user.getId(), user.getRandomCode()));
        map.put("user_id", user.getId());
        map.put("nickname", user.getNickname());
        map.put("status", user.getStatus());
        map.put("avatar", user.getAvatar());
        map.put("money", user.getMoney());
        map.put("balance", user.getBalance());
        map.put("mobile", user.getMobile());
        map.put("ext_money", user.getExtMoney());
        map.put("new_recharge_time", user.getNewRechargeTime());
        return success(map);
    }

    /**
     * 修改用户头像和昵称
     */
    @RequestMapping("editUser")
    @AuthCheck(LoginType.USER)
    @ResponseBody
    public StandardRtnDto<?> editUser(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String avatar = params.get("avatar");
        User user = UserContext.getUser();
        username = Base64.encode(username);
        if (StringUtils.isNotEmpty(username)) {
            user.setNickname(username);
        }
        if (StringUtils.isNotEmpty(avatar)) {
            user.setAvatar(avatar);
        }
        userService.update(user);
        return success("修改成功");

    }

    /**
     * 退出登录
     */
    @RequestMapping("logout")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> logout() {
        User user = UserContext.getUser();
        userService.update(user);

        return success("退出成功！");
    }

    /**
     * 修改头像(Y)  --修改頭像的問題暫緩
     */
    @RequestMapping("updateIcon")
    @AuthCheck(LoginType.USER)
    @ResponseBody
    public StandardRtnDto<?> updateIcon() {
        User user = UserContext.getUser();
        String avatar = ""; // 保存到本地服務器
        if (StringUtils.isNotEmpty(avatar)) {
            user.setAvatar(avatar);
            userService.update(user);
            return success("修改成功！");
        } else {
            return error("修改失败！");
        }
    }

    /**
     * 修改昵称(Y)
     */
    @RequestMapping("updateNickname")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> updateNickname(@RequestBody Map<String, String> params) {
        String nickname = params.get("nickname");
        ManageUser user = UserContext.getMUser();
        if (StringUtils.isEmpty(nickname)) {
            return error("昵称不能为空！");
        }
        user.setNickname(nickname);
//		user.set("username", nickname);
        manageUserService.update(user);
        return success("昵称修改成功！");
    }


    /**
     * 功能介绍
     */
    @RequestMapping("activityIntro")
    @ResponseBody
    public StandardRtnDto<?> activityIntro() {
        return success(activityIntroService.latest());
    }

    /**
     * 关于我们
     */
    @RequestMapping("aboutUs")
    @ResponseBody
    public StandardRtnDto<?> aboutUs() {
        ActivityIntro ai = activityIntroService.findById(1L);
        return success(ai);
    }

    /**
     * 消息列表
     */
    @RequestMapping("platformNoticeList")
    @ResponseBody
    public StandardRtnDto<?> platformNoticeList() {
        return success(platformNoticeService.activeList());
    }

}