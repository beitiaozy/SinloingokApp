package com.sinloingok.app.models.net4g;

import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.NextCodeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备订单表
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
@NoArgsConstructor
public class NetSiteOrder {

    /**
     *
     */
    private static final long serialVersionUID = 5968825670095069087L;

    private Long id;
    private String payCode; // 訂單編號
    private Long userId;// 用戶ID
    private String onlyCode;//設備編碼
    private String status; // 訂單狀態
    private BigDecimal totalAmount = new BigDecimal(0);//訂單總金額
    private BigDecimal discountAmount = new BigDecimal(0);//抵扣金額

    private BigDecimal netAmount = new BigDecimal(0); //實付金額
    private String beginTime;
    private String endTime;
    private Long voucherId; // 訂單關聯優惠券
    private String calc;//結算途徑
    private List<NetSiteOrderItemized> items;

    public Map<String, NetSiteOrderItemized> itemizedMap = new HashMap<>();

    public NetSiteOrder(NetSite netSite, long userId, long voucherId){
        setStatus(OrderStatus.USEING);
        setOnlyCode(netSite.getOnlyCode());
        setBeginTime(DateUtils.curTime());
        setEndTime(DateUtils.curTime());
        setPayCode(new DateUtils("yyMMddHHmmss").toString() + NextCodeUtils.nextVercode());
        setUserId(userId);
        setVoucherId(voucherId);
    }

    public void setDiscountAmount(BigDecimal discountAmount){
        this.discountAmount = discountAmount;
        BigDecimal actual = totalAmount.subtract(discountAmount);
        setNetAmount(actual.max(new BigDecimal(0)));
    }

    public void setTotalAmount(BigDecimal totalAmount){
        this.totalAmount = totalAmount;
        this.netAmount = totalAmount;
    }

    public String getLastOperationTime(){
        String lastOperateTime = beginTime;
        for(NetSiteOrderItemized itemized : items){
            long seconds = DateUtils.secondsDifference(itemized.getUpdateTime(), lastOperateTime);
            if(seconds > 0){
                lastOperateTime = itemized.getUpdateTime();
            }
        }
        return lastOperateTime;
    }

    public void setItems(List<NetSiteOrderItemized> items) {
        this.items = items;
        items.forEach(item -> itemizedMap.put(item.getRuleCode(), item));
    }

    public NetSiteOrderItemized getOrderItemized(String ruleCode){
        if(itemizedMap.size() == 0 && getItems().size() > 0){
            setItems(getItems());
        }
        return itemizedMap.get(ruleCode);
    }
}
