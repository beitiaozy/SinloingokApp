package com.sinloingok.app.service.user;

import com.google.common.collect.Lists;
import com.sinloingok.app.constant.BalanceType;
import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.dao.UserMoneyRecordDao;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.TransactionFlowType;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.user.RechargeRecordDto;
import com.sinloingok.app.dtos.user.UserMoneyRecordStatDto;
import com.sinloingok.app.dtos.user.UserMoneyStatsGroupedByPeriod;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.models.user.UserMoneyRecord;
import com.sinloingok.app.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserMoneyRecordService {

    @Autowired
    private UserMoneyRecordDao userMoneyRecordDao;
    @Autowired
    private UserService userService;

    public SimplePageDto<RechargeRecordDto> pageRechargeRecord(Long userId, int pageNo, int pageSize) {
        List<Integer> types = Arrays.asList(TransactionFlowType.RECHARGE.getCode(), TransactionFlowType.MONTHLY_CARD_RECHARGE.getCode(), TransactionFlowType.TIMES_CARD_RECHARGE.getCode());
        int offset = (pageNo - 1) * pageSize;
        long totalRecord = userMoneyRecordDao.countRechargeRecord(types, userId);
        List<RechargeRecordDto> list = userMoneyRecordDao.selectRechargeRecord(types, userId, offset, pageSize);
        list.forEach(dto -> {
            TransactionFlowType type = TransactionFlowType.fromCode(dto.getType());
            GenericRecharge recharge = GenericRecharge.fromCode(dto.getRemark());
            dto.addAttr("name", type.getName());
            dto.addAttr("description", recharge == null ? type.getDescription() : recharge.getDescription());
        });
        return new SimplePageDto<>(pageNo, pageSize, totalRecord, list);
    }

    public UserMoneyRecord findByMinOrderId(Long orderId) {
        return userMoneyRecordDao.selectByOrderIdGte(orderId);
    }

    /**
     * 創建用戶充值記錄
     */
    public void createUserMoneyRechargeRecord(RechargeOrder order, String message){

        User user = userService.findById(order.getUserId(), order.getAddressId());

        // 添加余额记录
        UserMoneyRecord record = new UserMoneyRecord();
        record.setUserId(user.getId());
        record.setShopId(3l);
        record.setAddressId(order.getAddressId());
        record.setRemark(BalanceType.RECHARGE);
        record.setMoney(order.getMoney());
        record.setAwards(order.getAwards());
        record.setBalance(user.getBalance());
        record.setExtMoney(user.getExtMoney());
        record.setType(BalanceType.RECHARGE);
        record.setOrderId(order.getId());
        record.setMessage(message);
        record.setCreateTime(DateUtils.curTime());
        userMoneyRecordDao.insertRechargeRecord(record);
    }

    public void createUserMoneyNetSiteOrderRecord(NetSiteOrder order, String message){

        User user = userService.findById(order.getUserId(), SysConstant.DEFAULT_ADDRESS);

        // 添加余额记录
        UserMoneyRecord record = new UserMoneyRecord();
        record.setShopId(3l);

        record.setAddressId(SysConstant.DEFAULT_ADDRESS);
        record.setRemark(BalanceType.CUSTOME);
        record.setMoney(order.getTotalAmount().floatValue());
        record.setAwards(0f);

        record.setUserId(user.getId());
        record.setBalance(user.getBalance());
        record.setExtMoney(user.getExtMoney());
        record.setType(BalanceType.CUSTOME);
        record.setOrderId(order.getId());
        record.setMessage(message);
        record.setCreateTime(DateUtils.curTime());
        userMoneyRecordDao.insertRechargeRecord(record);
    }

    public UserMoneyStatsGroupedByPeriod selectGroupedMoneyStats(){
        List<UserMoneyRecordStatDto> statDtos = userMoneyRecordDao.selectGroupedMoneyStats();
        return convertToPeriodStats(statDtos);
    }

    private UserMoneyStatsGroupedByPeriod convertToPeriodStats(List<UserMoneyRecordStatDto> statList) {
        UserMoneyStatsGroupedByPeriod result = new UserMoneyStatsGroupedByPeriod();
        result.setToday(new UserMoneyStatsGroupedByPeriod.PeriodStat());
        result.setYesterday(new UserMoneyStatsGroupedByPeriod.PeriodStat());
        result.setMonth(new UserMoneyStatsGroupedByPeriod.PeriodStat());

        for (UserMoneyRecordStatDto stat : statList) {
            String type = stat.getType(); // "消费"、"充值"等
            boolean isRecharge = BalanceType.RECHARGE.equals(type);
            boolean isConsume = BalanceType.CUSTOME.equals(type);

            // 今日
            if (stat.getTodayMoney() != null && stat.getTodayMoney() > 0) {
                UserMoneyStatsGroupedByPeriod.PeriodStat today = result.getToday();
                if (isRecharge) {
                    today.setRechargeMoney(today.getRechargeMoney() + stat.getTodayMoney());
                    today.setRechargeCount(today.getRechargeCount() + stat.getTodayCount());
                } else if (isConsume) {
                    today.setConsumeMoney(today.getConsumeMoney() + stat.getTodayMoney());
                    today.setConsumeCount(today.getConsumeCount() + stat.getTodayCount());
                }
            }

            // 昨日
            if (stat.getYesterdayMoney() != null && stat.getYesterdayMoney() > 0) {
                UserMoneyStatsGroupedByPeriod.PeriodStat yesterday = result.getYesterday();
                if (isRecharge) {
                    yesterday.setRechargeMoney(yesterday.getRechargeMoney() + stat.getYesterdayMoney());
                    yesterday.setRechargeCount(yesterday.getRechargeCount() + stat.getYesterdayCount());
                } else if (isConsume) {
                    yesterday.setConsumeMoney(yesterday.getConsumeMoney() + stat.getYesterdayMoney());
                    yesterday.setConsumeCount(yesterday.getConsumeCount() + stat.getYesterdayCount());
                }
            }

            // 本月
            if (stat.getMonthMoney() != null && stat.getMonthMoney() > 0) {
                UserMoneyStatsGroupedByPeriod.PeriodStat month = result.getMonth();
                if (isRecharge) {
                    month.setRechargeMoney(month.getRechargeMoney() + stat.getMonthMoney());
                    month.setRechargeCount(month.getRechargeCount() + stat.getMonthCount());
                } else if (isConsume) {
                    month.setConsumeMoney(month.getConsumeMoney() + stat.getMonthMoney());
                    month.setConsumeCount(month.getConsumeCount() + stat.getMonthCount());
                }
            }
        }

        return result;
    }
}
