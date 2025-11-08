package com.sinloingok.app.service.user;

import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dao.RechargeOrderDao;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.user.RechargeOrderResDto;
import com.sinloingok.app.dtos.user.RechargeRecordDto;
import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service layer for RechargeOrder operations.
 */
@Service
public class RechargeOrderService {
    @Autowired
    private RechargeOrderDao rechargeOrderDao;

    public SimplePageDto<RechargeOrderResDto> queryRechargeOrderResDto(PageData<RechargeRecordDto> pageData) {
        int totalRecord = rechargeOrderDao.countRechargeOrder(pageData.getBody(), pageData);
        List<RechargeOrderResDto> list = rechargeOrderDao.selectRechargeOrderByPage(pageData.getBody(), pageData);
        return new SimplePageDto<>(pageData.getPageNo(), pageData.getPageSize(), totalRecord, list);
    }

    public Map<String, Object> sumMoneyAwards(RechargeRecordDto dto) {
        return rechargeOrderDao.sumMoneyAwards(dto);
    }

    public RechargeOrder findByCodeAndUser(String code, Long userId) {
        return rechargeOrderDao.selectByCodeAndUser(code, userId);
    }

    public RechargeOrder findLatestByUserAndStatus(Long userId, String status) {
        return rechargeOrderDao.selectLatestByUserAndStatus(userId, status);
    }

    public void insert(RechargeOrder order) {
        rechargeOrderDao.insert(order);
    }

    public long createChargeOrder(User user, BluetoothAddressRecharge recharge, String onlyCode) {
        String code = DateUtils.generateUniqueRandomNumber() + user.getId();
        RechargeOrder order = new RechargeOrder();
        order.setCode(code);
        order.setUserId(user.getId());
        order.setRechargeId(recharge.getId());
        order.setType(recharge.getType());
        order.setOnlyCode(onlyCode);
        order.setAddressId(recharge.getAddressId());
        order.setStatus("未充值");
        order.setCreateTime(DateUtils.curTime());
        order.setMoney(recharge.getMoney());
        order.setAwards(recharge.getAwards());
        rechargeOrderDao.insert(order);
        return order.getId();
    }

    public RechargeOrder findById(Long id) {
        return rechargeOrderDao.selectById(id);
    }

    public void update(RechargeOrder order) {
        rechargeOrderDao.update(order);
    }
}
