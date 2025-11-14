package com.sinloingok.app.dtos.smyoo.req;

import com.sinloingok.app.constant.CommonParams;
import lombok.Data;

/**
 * 思麓接口請求公共基類。
 */
@Data
public class RequestBase {

    private CommonParams common;

    public RequestBase() {
    }

    public RequestBase(CommonParams common) {
        this.common = common;
    }
}

