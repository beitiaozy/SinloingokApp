package com.sinloingok.app.service.netsite;

import com.sinloingok.app.dao.BluetoothAddressRechargeDao;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BluetoothAddressRechargeService {

    @Autowired
    private BluetoothAddressRechargeDao recharge2Dao;

    /**
     * 用于管理端查询并修改
     * @param addressId
     * @return
     */
    public List<BluetoothAddressRecharge> rechargeList(Long addressId) {
        List<BluetoothAddressRecharge> listRecharge = recharge2Dao.rechargeListWithNewRecord(null, addressId);
        return listRecharge;
    }

    public List<BluetoothAddressRecharge> rechargeOptionDtoList(User user, long addressId){
        List<BluetoothAddressRecharge> listRecharge = recharge2Dao.rechargeListWithNewRecord(DateUtils.curTime().substring(0, 10), addressId);
        // 先处理所有数据
        for (BluetoothAddressRecharge data : listRecharge) {
            if (GenericRecharge.NEWCOMER.isValid(data.getType())) {
                data.setExpirationDate(user.getNewRechargeTime().substring(0, 10));
            }
            handleRechargeStrategy(data);
        }
        // 然后过滤掉天数为负的记录 以及未被启用的数据
        listRecharge.removeIf(data -> {
            int days = Integer.parseInt(data.getRemainingDays());
            return days <= 0 || data.getEnable() == 1;
        });
        return listRecharge;
    }

    public void insertRecharge(BluetoothAddressRecharge recharge) {
        recharge.setCreateTime(DateUtils.curTime());
        if(StringUtils.isEmpty(recharge.getExpirationDate())){
            recharge.setExpirationDate("2099-12-31");
        }
        handleRechargeStrategy(recharge);
        recharge.setEnable(0);
        recharge2Dao.insertBluetoothAddressRecharge(recharge);
    }

    public void updateRecharge(BluetoothAddressRecharge recharge) {
        if(StringUtils.isNotEmpty(recharge.getExpirationDate())){
            handleRechargeStrategy(recharge);
        }
        recharge2Dao.updateBluetoothAddressRecharge(recharge);
    }

    public void deleteRecharge(Long id) {
        recharge2Dao.deleteById(id);
    }

    public BluetoothAddressRecharge findRechargeById(Long id) {
        return recharge2Dao.selectById(id);
    }

    /**
     * 充值类套餐计算有效期和剩余天数展示
     * @param recharge
     */
    private void handleRechargeStrategy(BluetoothAddressRecharge recharge){
        if(GenericRecharge.isBalanceRechargeStrategy(recharge.getType().toUpperCase())){
            int days = DateUtils.daysBetween2(DateUtils.todayDate(0), recharge.getExpirationDate());
            recharge.setRemainingDays(String.valueOf(days));
        }
        if(GenericRecharge.isComboRechargeStrategy(recharge.getType().toUpperCase())){
            int days = Integer.valueOf(recharge.getRemainingDays());
            recharge.setExpirationDate(DateUtils.todayDate(days));
        }
    }
}
