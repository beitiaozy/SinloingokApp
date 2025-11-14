package com.sinloingok.app.models.payment;

import lombok.Data;

import java.util.Date;

@Data
public class TerminalCredential {
    private Long id;
    private Long addressId;
    private String onlyCode;
    private Long paymentAccountId;
    private String terminalSn;
    private String terminalKey;
    private Integer isEnabled;
    private Date gmtCreate;
    private Date gmtModified;
}
