package com.sinloingok.app.dtos.smyoo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinloingok.app.constant.CommonParams;
import com.sinloingok.app.constant.Endpoints;
import com.sinloingok.app.dtos.smyoo.req.*;
import com.sinloingok.app.util.smyoo.HttpTransport;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SmyooClient {
    private final HttpTransport http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SmyooClient(HttpTransport http) {
        this.http = http;
    }

    public void setBpeSessionId(String bpeSessionId){
        http.setBpeSessionId(bpeSessionId);
    }

    // ---------- 通用发送 ----------
    private <T> ApiResponse<T> post(String url, Map<String, Object> body, TypeReference<ApiResponse<T>> type)
            throws IOException {
        String json = JSONObject.toJSONString(body);
        try (Response resp = http.postJson(url, json)) {
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + " for " + url);
            }
            return mapper.readValue(resp.body().byteStream(), type);
        }
    }

    private Map<String, Object> base(CommonParams c) {
        Map<String, Object> m = new HashMap<>();
        m.put("appId", c.getAppId());
        m.put("areaId", c.getAreaId());
        m.put("endpointOS", c.getEndpointOS());
        m.put("clientVersion", c.getClientVersion());
        m.put("endpointIP", c.getEndpointIP());
        m.put("dataTag", c.getDataTag());
        m.put("context", c.getContext());
        m.put("locale", c.getLocale());
        m.put("deviceId", c.getDeviceId());
        m.put("client_id", c.getClient_id());
        m.put("client_secret", c.getClient_secret());
        return m;
    }

    // ---------- 3.1.1 登录（重点接口） ----------
    public ApiResponse<Map<String, Object>> login(LoginRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("phone", req.getPhone());
        m.put("password", req.getPassword());
        m.put("autologin", req.getAutologin());
        m.put("client_secret", req.getClient_secret());
        // 返回 data 中有 ticket / userid / autokey 等
        return post(Endpoints.LOGIN, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.2 验证票据（重点接口） ----------
    public ApiResponse<Map<String, Object>> verifyTicket(TicketVerifyRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        if (req.getTicket() != null) m.put("ticket", req.getTicket());
        return post(Endpoints.VERIFY_TICKET, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.3 获取设备信息是否变化 ----------
    public ApiResponse<Map<String, Object>> statusChanged(RequestBase req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        return post(Endpoints.STATUS_CHANGED, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.4 获取所有设备信息 ----------
    public ApiResponse<Map<String, Object>> queryDevices(RequestBase req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        return post(Endpoints.QUERY_DEVICES, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.5 通过设备名获取 mcuid ----------
    public ApiResponse<Map<String, Object>> queryMcuids(McuNameQueryRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuname", req.getMcuname());
        return post(Endpoints.QUERY_MCU_IDS, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.6 获取设备状态 ----------
    public ApiResponse<Map<String, Object>> getDeviceData(McuidRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        return post(Endpoints.GET_DEVICE_DATA, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.7 设置设备状态 ----------
    public ApiResponse<Map<String, Object>> setDeviceData(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datatype", req.getDatatype());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SET_DEVICE_DATA, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.8 设置单个通道 ----------
    public ApiResponse<Map<String, Object>> setChannelData(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datatype", req.getDatatype());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SET_CHANNEL_DATA, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.9 设置单个通道(可联动自动化/智能场景) ----------
    public ApiResponse<Map<String, Object>> setChannelDataAuto(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datatype", req.getDatatype());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SET_CHANNEL_DATA_AUTO, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.10 设置多路设备状态 ----------
    public ApiResponse<Map<String, Object>> setMultiChannels(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SET_MULTI_CHANNELS, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.11 获取设备信息 ----------
    public ApiResponse<Map<String, Object>> getMcuInfo(McuidRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        return post(Endpoints.GET_MCU_INFO, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.12 获取红外空调设备状态 ----------
    public ApiResponse<Map<String, Object>> irGetData(McuidRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        return post(Endpoints.IR_GET_DATA, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.13 设置红外空调设备状态 ----------
    public ApiResponse<Map<String, Object>> irSetData(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datatype", req.getDatatype());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.IR_SET_DATA, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.14 设置自定义红外设备状态 ----------
    public ApiResponse<Map<String, Object>> irSetDataIrFile(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datatype", req.getDatatype());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.IR_SET_DATA_IRFILE, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.15 获取红外子设备信息 ----------
    public ApiResponse<Map<String, Object>> irDeviceInfo(McuidRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        return post(Endpoints.IR_DEVICE_INFO, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.16 获取红外子设备列表 ----------
    public ApiResponse<Map<String, Object>> irDeviceList(ParentIdRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("parentid", req.getParentid());
        return post(Endpoints.IR_DEVICE_LIST, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.17 云喇叭播放记录列表 ----------
    public ApiResponse<Map<String, Object>> speakerList(McuidRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        return post(Endpoints.SPEAKER_LIST, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.18 添加云喇叭记录 ----------
    public ApiResponse<Map<String, Object>> speakerAdd(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_ADD, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.19 删除云喇叭记录 ----------
    public ApiResponse<Map<String, Object>> speakerDel(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_DEL, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.20 播放云喇叭记录 ----------
    public ApiResponse<Map<String, Object>> speakerPlay(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_PLAY, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.21 云喇叭直接播放文字 ----------
    public ApiResponse<Map<String, Object>> speakerPlayText(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_PLAY_TEXT, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.22 云喇叭直接播放长文本 ----------
    public ApiResponse<Map<String, Object>> speakerPlayLongText(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_PLAY_LONGTEXT, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.23 云喇叭播放指定音频文件 ----------
    public ApiResponse<Map<String, Object>> speakerPlayFile(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_PLAY_FILE, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.24 云喇叭播放外部音频 ----------
    public ApiResponse<Map<String, Object>> speakerPlayOnline(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_PLAY_ONLINE, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.25 倒计时闹钟（走 setdevicedata） ----------
    public ApiResponse<Map<String, Object>> setCountdownAlarm(DatapointRequest req) throws IOException {
        // datapoint: {"sec":10800,"alarm":"1800,900,300,0","alarmtype":0|1}
        return setDeviceData(req);
    }

    // ---------- 3.1.26 云喇叭播放 SD 文件 ----------
    public ApiResponse<Map<String, Object>> speakerSdFile(DatapointRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("mcuid", req.getMcuid());
        m.put("datatype", req.getDatatype()); // 1..8
        m.put("datapoint", req.getDatapoint());
        return post(Endpoints.SPEAKER_SD_FILE, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }

    // ---------- 3.1.27 获取网关子设备列表 ----------
    public ApiResponse<Map<String, Object>> gwDeviceList(ParentIdRequest req) throws IOException {
        Map<String, Object> m = base(req.getCommon());
        m.put("parentid", req.getParentid());
        return post(Endpoints.GW_DEVICE_LIST, m, new TypeReference<ApiResponse<Map<String, Object>>>(){});
    }
}
