package com.sinloingok.app.service.user;

import com.sinloingok.app.dao.UserVoucherMapper;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.StatusVoucher;
import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import com.sinloingok.app.models.voucher.UserVoucher;
import com.sinloingok.app.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserVoucherService {
    @Autowired
    private UserVoucherMapper voucherMapper;

    public List<UserVoucher> listUserVoucher(UserVoucher voucher) {
        voucher.setStatus(StatusVoucher.ACTIVE.getCode());
        List<UserVoucher> vouchers = voucherMapper.selectByVoucher(voucher);
        return vouchers;
    }

    public UserVoucher findById(Long id) {
        return voucherMapper.selectVoucherById(id);
    }


    public void createVoucher(BluetoothAddressRecharge recharge, long userId, String payCode) {
        UserVoucher voucher = new UserVoucher();
        voucher.setUserId(userId);
        voucher.setPayCode(payCode);
        voucher.setStatus(StatusVoucher.ACTIVE.getCode());
        voucher.setAddressId(recharge.getAddressId());
        voucher.setAmount(recharge.getAwards().intValue());
        // @TODO 此處暫定為小於10次的默認為洗車，大於等於10次的默認為電爐
        if (voucher.getAmount() < 10) {
            voucher.setTotalMoney(20f);
        } else {
            voucher.setTotalMoney(4f);
        }
        voucher.setCreateTime(DateUtils.todayDate(0));
        if (GenericRecharge.MONTH_COMBO.isValid(recharge.getType().toUpperCase())) {
            voucher.setExpireDate(DateUtils.todayDate(30));
        }
        if (GenericRecharge.DEAL_COMBO.isValid(recharge.getType().toUpperCase())) {
            voucher.setExpireDate(DateUtils.todayDate(60));
        }
        voucher.setUseTime(DateUtils.todayDate(0));
        voucher.setFinishTime(voucher.getExpireDate());
        voucher.setType(recharge.getType());
        insert(voucher);
    }

    public void insert(UserVoucher voucher) {
        voucherMapper.insert(voucher);
    }

    public void update(UserVoucher voucher) {
        voucherMapper.update(voucher);
    }
}
