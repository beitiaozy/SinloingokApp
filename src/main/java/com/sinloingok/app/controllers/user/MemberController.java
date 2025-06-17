package com.sinloingok.app.controllers.user;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.UserAccountDto;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.custom.ConsumptionService;
import com.sinloingok.app.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员信息接口
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@RestController
@RequestMapping("member")
public class MemberController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConsumptionService consumptionService;

    /**
     * 查询会员信息
     */
    @RequestMapping("memberList")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public SimplePageDto memberList(@RequestBody PageData<UserAccountDto> pageData) {
        SimplePageDto<User> result = userService.memberList(pageData.getBody(), (int) pageData.getOffset(), pageData.getPageSize());
        return result;
    }

    /**
     * 会员修改
     */
    @RequestMapping("editMember")
    @ResponseBody
    public StandardRtnDto<?> editMember(@RequestBody UserAccountDto account) {
        consumptionService.updateBalance(account);
        return success("修改余额成功");
    }
}
