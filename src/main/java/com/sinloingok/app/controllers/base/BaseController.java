package com.sinloingok.app.controllers.base;

import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.kits.MD5;

import java.math.BigDecimal;

/**
 * 封装一些公用方法的基础Controller
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
public class BaseController {


    /**
     * 返回错误提示信息
     *
     * @param message
     */
    public StandardRtnDto<String> error(String message) {
        StandardRtnDto<String> dto = new StandardRtnDto<>(SysConstant.ResultCode.ERROR, message);
        return dto;
    }


    /**
     * 返回成功消息
     *
     * @param message
     */
    public StandardRtnDto<String> success(String message) {
        return new StandardRtnDto<>(SysConstant.ResultCode.SUCCESS, message);
    }

    protected <T> StandardRtnDto<T> success(T message) {
        return new StandardRtnDto<T>(SysConstant.ResultCode.SUCCESS, message);
    }

    /**
     * 格式化金额
     *
     * @param total_money
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Float formatMoney(Float total_money) {
        int scale = 2;// 设置位数
        int roundingMode = 4;// 表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        BigDecimal bd = new BigDecimal((double) (total_money));
        bd = bd.setScale(scale, roundingMode);
        total_money = bd.floatValue();

        return total_money;
    }
}
