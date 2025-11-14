package com.sinloingok.app.dtos.smyoo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int resultCode;
    private String resultMsg;
    private String dataTag;
    private String context;
    private T data;

    public boolean isOk() { return resultCode == 0; }
    // getter/setter ...
}
