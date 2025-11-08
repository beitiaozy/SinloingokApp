package com.sinloingok.app.dtos.smyoo.req;

import com.sinloingok.app.constant.CommonParams;
import lombok.Data;

@Data
public class LoginRequest extends RequestBase {

    private String phone;
    private String password;
    private String client_secret;

    public LoginRequest() {
    }

    public LoginRequest(CommonParams common) {
        super(common);
    }
}

