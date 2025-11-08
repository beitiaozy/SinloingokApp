package com.sinloingok.app.models.net4g;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 4G联网设备
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class NetSite {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String onlyCode;
    private String name;
    private Long addressId;
    private String terminalSn;
    private String terminalKey;
    private String status;
    private String createTime;
    private String updateTime;
    private String heartTime;

    private long paymentAccountId;

    /**
     *  PM_NET_SITE, WSC_NET_SITE,KY_NET_SITE  泡沫網絡控制器，洗車機網絡控製器和空壓機網絡控製器
     */
    private String purpose;
    /**
     * 每半小時單價
     */
    private BigDecimal baseUnitAmount;


    public NetSite(String only_code) {
        this.setOnlyCode(only_code);
    }

    public void refresh(String current) {
        this.setHeartTime(current);
        this.setUpdateTime(current);
    }

    // Getter 和 Setter 方法
    public void mapFromRecord(NetSite record) {
        if (record == null) {
            return;
        }
        this.setId(record.getId());
        this.setOnlyCode(record.getOnlyCode());
        this.setName(record.getName());
        this.setAddressId(record.getAddressId());
        this.setTerminalSn(record.getTerminalSn());
        this.setTerminalKey(record.getTerminalKey());
        this.setStatus(record.getStatus());
        this.setCreateTime(record.getCreateTime());
        this.setUpdateTime(record.getUpdateTime());
        this.setHeartTime(record.getHeartTime());
        this.setBaseUnitAmount(record.getBaseUnitAmount());
    }


}
