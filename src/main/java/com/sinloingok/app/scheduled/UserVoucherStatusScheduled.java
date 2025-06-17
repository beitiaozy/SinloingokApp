package com.sinloingok.app.scheduled;

import java.util.List;

import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.StatusVoucher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.sinloingok.app.models.voucher.UserVoucher;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.service.user.UserVoucherService;

/**
 *  优惠券状态检查清理
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Slf4j
@Component
public class UserVoucherStatusScheduled {

    @Scheduled(cron = "0 1 0 * * ?") // 每天00:01:00执行
    public void execute() {
        try {
            voucherOver();
        } catch (Exception e) {
            log.error("OrderOverJob error", e);
        }
    }

    /**
     * 优惠券自动到期
     *
     */
    public static void voucherOver() {
        UserVoucherService voucherService = SBeanUtils.getBean(UserVoucherService.class);
        UserVoucher voucher = new UserVoucher();
        voucher.setStatus(StatusVoucher.ACTIVE.getCode());
        // 查询所有有效的
        List<UserVoucher> list = voucherService.listUserVoucher(voucher);
        for (UserVoucher v : list) {
            String today = DateUtils.todayDate(0);
            if(today.equals(v.getExpireDate())){
                v.setStatus(StatusVoucher.EXPIRED.getCode());
            }
            if(GenericRecharge.isComboRechargeStrategy(voucher.getType())){
                voucher.setUseTime(today);
            }
            voucherService.update(v);

        }
    }
}
