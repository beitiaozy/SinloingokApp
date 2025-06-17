package com.sinloingok.app.service.custom.strategy;

import com.sinloingok.app.models.fund.UserAddressAccount;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumptionContext {
    /** 当前充值订单 */
    private RechargeOrder order;

    /** 当前用户 */
    private User user;

    /** 自定义字段，可扩展策略逻辑 */
    private Map<String, Object> attributes = new HashMap<>();

    public ConsumptionContext(RechargeOrder order, User user) {
        this.order = order;
        this.user = user;
    }

    // 可扩展方法：
    public void setAttr(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(String key, Class<T> clazz) {
        Object val = attributes.get(key);
        return clazz.isInstance(val) ? (T) val : null;
    }
}
