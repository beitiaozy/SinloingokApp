package com.sinloingok.app.dtos.order;

import com.sinloingok.app.dtos.DateRangeQuery;
import lombok.Data;

/**
 * 根據設備和人員常用信息查詢數據
 */
@Data
public class NetSiteUserReqDto extends DateRangeQuery {

    private String mobile;

    private String addressId;

    private long userId;

    private String orderStatus;

    private String payCode;
}
