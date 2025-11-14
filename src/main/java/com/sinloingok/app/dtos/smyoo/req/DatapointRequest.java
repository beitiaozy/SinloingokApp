package com.sinloingok.app.dtos.smyoo.req;

import com.sinloingok.app.constant.CommonParams;
import lombok.Data;

@Data
public class DatapointRequest extends RequestBase {

    private String mcuid;
    private Integer datatype;
    private String datapoint;

    public DatapointRequest() {
    }

    public DatapointRequest(CommonParams common) {
        super(common);
    }
}

