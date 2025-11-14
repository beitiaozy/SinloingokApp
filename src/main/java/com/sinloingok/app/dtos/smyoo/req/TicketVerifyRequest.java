package com.sinloingok.app.dtos.smyoo.req;

import com.sinloingok.app.constant.CommonParams;
import lombok.Data;

@Data
public class TicketVerifyRequest extends RequestBase {

    private String ticket;

    public TicketVerifyRequest() {
    }

    public TicketVerifyRequest(CommonParams common) {
        super(common);
    }
}

