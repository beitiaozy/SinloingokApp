package com.sinloingok.app.models.net4g;

import lombok.Data;

/**
 * 订单中的脉冲相关计费
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class NetSiteOrderDetail {

        private static final long serialVersionUID = 5968825670095069087L;

        private Long id;
        private Long orderId;
        private Integer type;
        private String beginTime;
        private String endTime;

}

