package com.sinloingok.app.controllers.pay;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.models.voucher.UserVoucher;
import com.sinloingok.app.service.user.UserVoucherService;
import com.sinloingok.app.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 代金券控製類
 */
@RestController
@RequestMapping("userVoucher")
public class VoucherController extends BaseController {

    @Autowired
    private UserVoucherService userVoucherService;


    /**
     * 查看自己的洗车券列表
     *
     */
    @RequestMapping("voucherList")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> voucherList(@RequestBody UserVoucher voucher) {
        User user = UserContext.getUser();
        voucher.setUserId(user.getId());
        return success(userVoucherService.listUserVoucher(voucher));
    }
}
