package com.sinloingok.app.dtos.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NetSiteOrderResDto {

    /** 訂單ID */
    private Long orderId;

    /** 唯一碼 */
    private String onlyCode;

    /** 支付碼 */
    private String payCode;

    /** 訂單狀態 */
    private String status;

    /** 總金額 */
    private BigDecimal totalAmount;

    /** 折扣金額 */
    private BigDecimal discountAmount;

    /** 實付金額（淨額） */
    private BigDecimal netAmount;

    /** 開始時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /** 結束時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 場地名稱 */
    private String siteName;

    /** 用戶暱稱 */
    private String nickname;

    /** 用戶手機 */
    private String mobile;

    /** 地址名稱 */
    private String addrName;
}
