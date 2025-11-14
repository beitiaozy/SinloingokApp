package com.sinloingok.app.voice;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.TestSinloingokApplication;
import com.sinloingok.app.constant.CommonParams;
import com.sinloingok.app.dtos.smyoo.ApiResponse;
import com.sinloingok.app.dtos.smyoo.SmyooClient;
import com.sinloingok.app.dtos.smyoo.req.DatapointRequest;
import com.sinloingok.app.dtos.smyoo.req.LoginRequest;
import com.sinloingok.app.dtos.smyoo.req.McuidRequest;
import com.sinloingok.app.dtos.smyoo.req.McuNameQueryRequest;
import com.sinloingok.app.dtos.smyoo.req.ParentIdRequest;
import com.sinloingok.app.dtos.smyoo.req.RequestBase;
import com.sinloingok.app.dtos.smyoo.req.TicketVerifyRequest;
import com.sinloingok.app.service.VoicePromptService;
import com.sinloingok.app.util.smyoo.DatapointBuilder;
import com.sinloingok.app.util.smyoo.HttpTransport;
import com.sinloingok.app.util.smyoo.ParamsFactory;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestVoicePromptService extends TestSinloingokApplication {

    private static final SmyooApiFixture API = new SmyooApiFixture();

    @Autowired
    private VoicePromptService voicePromptService;

    @BeforeClass
    public static void setUpFixture() throws IOException {
        API.initialize();
    }

    private static void logResponse(String action, ApiResponse<?> response) {
        System.out.println("[" + action + "] => " + JSONObject.toJSONString(response));
    }

    @Test
    public void test01GetPrompt() {
        Map<String, String> data = Collections.singletonMap("nickname", "阿豪");
        String msg = voicePromptService.getPrompt("B03", data);
        assertNotNull(msg);
    }

    @Test
    public void test10StatusChanged() throws IOException {
        ApiResponse<Map<String, Object>> response = API.statusChanged();
        logResponse("statusChanged", response);
        assertNotNull(response);
    }

    @Test
    public void test11QueryDevices() throws IOException {
        ApiResponse<Map<String, Object>> response = API.queryDevices();
        logResponse("queryDevices", response);
        assertNotNull(response);
        assertFalse(API.getDeviceMaps().isEmpty());
    }

    @Test
    public void test12QueryMcuids() throws IOException {
        Assume.assumeTrue("缺少設備名稱，跳過測試", API.getSampleMcuname() != null);
        ApiResponse<Map<String, Object>> response = API.queryMcuids(API.getSampleMcuname());
        logResponse("queryMcuids", response);
        assertNotNull(response);
    }

    @Test
    public void test13GetDeviceData() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.getDeviceData(API.getSampleMcuid());
        logResponse("getDeviceData", response);
        assertNotNull(response);
    }

    @Test
    public void test14SetDeviceData() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.setDeviceData(API.getSampleMcuid());
        logResponse("setDeviceData", response);
        assertNotNull(response);
    }

    @Test
    public void test15SetChannelData() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.setChannelData(API.getSampleMcuid());
        logResponse("setChannelData", response);
        assertNotNull(response);
    }

    @Test
    public void test16SetChannelDataAuto() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.setChannelDataAuto(API.getSampleMcuid());
        logResponse("setChannelDataAuto", response);
        assertNotNull(response);
    }

    @Test
    public void test17SetMultiChannels() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.setMultiChannels(API.getSampleMcuid());
        logResponse("setMultiChannels", response);
        assertNotNull(response);
    }

    @Test
    public void test18GetMcuInfo() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.getMcuInfo(API.getSampleMcuid());
        logResponse("getMcuInfo", response);
        assertNotNull(response);
    }

    @Test
    public void test19IrGetData() throws IOException {
        Assume.assumeTrue("缺少紅外設備，跳過測試", API.getIrMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.irGetData(API.getIrMcuid());
        logResponse("irGetData", response);
        assertNotNull(response);
    }

    @Test
    public void test20IrSetData() throws IOException {
        Assume.assumeTrue("缺少紅外設備，跳過測試", API.getIrMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.irSetData(API.getIrMcuid());
        logResponse("irSetData", response);
        assertNotNull(response);
    }

    @Test
    public void test21IrSetDataIrFile() throws IOException {
        Assume.assumeTrue("缺少紅外設備，跳過測試", API.getIrMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.irSetDataIrFile(API.getIrMcuid());
        logResponse("irSetDataIrFile", response);
        assertNotNull(response);
    }

    @Test
    public void test22IrDeviceInfo() throws IOException {
        Assume.assumeTrue("缺少紅外設備，跳過測試", API.getIrMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.irDeviceInfo(API.getIrMcuid());
        logResponse("irDeviceInfo", response);
        assertNotNull(response);
    }

    @Test
    public void test23IrDeviceList() throws IOException {
        Assume.assumeTrue("缺少父設備ID，跳過測試", API.getGatewayParentId() != null);
        ApiResponse<Map<String, Object>> response = API.irDeviceList(API.getGatewayParentId());
        logResponse("irDeviceList", response);
        assertNotNull(response);
    }

    @Test
    public void test24SpeakerList() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.speakerList();
        logResponse("speakerList", response);
        assertNotNull(response);
    }

    @Test
    public void test25SpeakerAdd() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        String recordId = API.createSpeakerRecord("自助洗車歡迎您");
        assertNotNull("添加喇叭記錄失敗", recordId);
    }

    @Test
    public void test26SpeakerPlay() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        String recordId = Optional.ofNullable(API.getCachedSpeakerRecordId())
                .orElseGet(() -> {
                    try {
                        return API.createSpeakerRecord("洗車請注意安全");
                    } catch (IOException e) {
                        return null;
                    }
                });
        Assume.assumeTrue("沒有可播放的記錄", recordId != null);
        ApiResponse<Map<String, Object>> response = API.speakerPlay(recordId);
        logResponse("speakerPlay", response);
        assertNotNull(response);
    }

    @Test
    public void test27SpeakerDel() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        String recordId = Optional.ofNullable(API.getCachedSpeakerRecordId())
                .orElseGet(() -> {
                    try {
                        return API.createSpeakerRecord("臨時刪除測試");
                    } catch (IOException e) {
                        return null;
                    }
                });
        Assume.assumeTrue("沒有可刪除的記錄", recordId != null);
        ApiResponse<Map<String, Object>> response = API.speakerDel(recordId);
        logResponse("speakerDel", response);
        assertNotNull(response);
    }

    @Test
    public void test28SpeakerPlayText() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.speakerPlayText("歡迎使用洗涤服務");
        logResponse("speakerPlayText", response);
        assertNotNull(response);
    }

    @Test
    public void test29SpeakerPlayLongText() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.speakerPlayLongText("感謝您選擇洗涤服務，祝您使用愉快，謝謝光臨");
        logResponse("speakerPlayLongText", response);
        assertNotNull(response);
    }

    @Test
    public void test30SpeakerPlayFile() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.speakerPlayFile("0001");
        logResponse("speakerPlayFile", response);
        assertNotNull(response);
    }

    @Test
    public void test31SpeakerPlayOnline() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.speakerPlayOnline("https://example.com/test.mp3");
        logResponse("speakerPlayOnline", response);
        assertNotNull(response);
    }

    @Test
    public void test32SetCountdownAlarm() throws IOException {
        Assume.assumeTrue("缺少 mcuid，跳過測試", API.getSampleMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.setCountdownAlarm(API.getSampleMcuid());
        logResponse("setCountdownAlarm", response);
        assertNotNull(response);
    }

    @Test
    public void test33SpeakerSdFile() throws IOException {
        Assume.assumeTrue("缺少喇叭設備，跳過測試", API.getSpeakerMcuid() != null);
        ApiResponse<Map<String, Object>> response = API.speakerSdFile();
        logResponse("speakerSdFile", response);
        assertNotNull(response);
    }

    @Test
    public void test34GwDeviceList() throws IOException {
        Assume.assumeTrue("缺少父設備ID，跳過測試", API.getGatewayParentId() != null);
        ApiResponse<Map<String, Object>> response = API.gwDeviceList(API.getGatewayParentId());
        logResponse("gwDeviceList", response);
        assertNotNull(response);
    }

    private static class SmyooApiFixture {
        private static final int APP_ID = 1314;
        private static final int ENDPOINT_OS = 1;
        private static final String CLIENT_ID = "86922127";
        private static final String CLIENT_SECRET = "EF4DDE0AC00F44939BF6FCFCBC213EC1";
        private static final String DEVICE_ID = "82CA48CC5D2A4959BE1CFFE99974D924";
        private static final String PHONE = "18382051045";
        private static final String PASSWORD = "Dxpbl1904";

        private SmyooClient client;
        private CommonParams templateCommon;
        private Map<String, Object> loginData = Collections.emptyMap();
        private Map<String, Object> devicePayload = Collections.emptyMap();
        private List<Map<String, Object>> deviceMaps = new ArrayList<>();
        private String sampleMcuid;
        private String sampleMcuname;
        private String speakerMcuid;
        private String irMcuid;
        private String gatewayParentId;
        private String cachedSpeakerRecordId;

        void initialize() throws IOException {
            client = new SmyooClient(new HttpTransport());
            templateCommon = createCommonParams();
            loginData = login();
            verifyTicket();
            refreshDevices();
        }

        ApiResponse<Map<String, Object>> statusChanged() throws IOException {
            return client.statusChanged(new RequestBase(copyCommon()));
        }

        ApiResponse<Map<String, Object>> queryDevices() throws IOException {
            ApiResponse<Map<String, Object>> response = client.queryDevices(new RequestBase(copyCommon()));
            if (response != null && response.isOk()) {
                devicePayload = Optional.ofNullable(response.getData()).orElse(Collections.emptyMap());
                deviceMaps = collectDeviceMaps(devicePayload);
            }
            return response;
        }

        ApiResponse<Map<String, Object>> queryMcuids(String mcuname) throws IOException {
            McuNameQueryRequest request = new McuNameQueryRequest(copyCommon());
            request.setMcuname(mcuname);
            return client.queryMcuids(request);
        }

        ApiResponse<Map<String, Object>> getDeviceData(String mcuid) throws IOException {
            McuidRequest request = new McuidRequest(copyCommon());
            request.setMcuid(mcuid);
            return client.getDeviceData(request);
        }

        ApiResponse<Map<String, Object>> setDeviceData(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("index", 1)
                    .put("status", 1)
                    .json());
            return client.setDeviceData(request);
        }

        ApiResponse<Map<String, Object>> setChannelData(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("index", 1)
                    .put("status", 1)
                    .json());
            return client.setChannelData(request);
        }

        ApiResponse<Map<String, Object>> setChannelDataAuto(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("index", 1)
                    .put("status", 1)
                    .json());
            return client.setChannelDataAuto(request);
        }

        ApiResponse<Map<String, Object>> setMultiChannels(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatapoint(DatapointBuilder.create()
                    .put("channels", singleChannelPayload())
                    .json());
            return client.setMultiChannels(request);
        }

        ApiResponse<Map<String, Object>> getMcuInfo(String mcuid) throws IOException {
            McuidRequest request = new McuidRequest(copyCommon());
            request.setMcuid(mcuid);
            return client.getMcuInfo(request);
        }

        ApiResponse<Map<String, Object>> irGetData(String mcuid) throws IOException {
            McuidRequest request = new McuidRequest(copyCommon());
            request.setMcuid(mcuid);
            return client.irGetData(request);
        }

        ApiResponse<Map<String, Object>> irSetData(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("mode", "cool")
                    .put("temp", 25)
                    .json());
            return client.irSetData(request);
        }

        ApiResponse<Map<String, Object>> irSetDataIrFile(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("fileId", "demo-ir")
                    .json());
            return client.irSetDataIrFile(request);
        }

        ApiResponse<Map<String, Object>> irDeviceInfo(String mcuid) throws IOException {
            McuidRequest request = new McuidRequest(copyCommon());
            request.setMcuid(mcuid);
            return client.irDeviceInfo(request);
        }

        ApiResponse<Map<String, Object>> irDeviceList(String parentId) throws IOException {
            ParentIdRequest request = new ParentIdRequest(copyCommon());
            request.setParentid(parentId);
            return client.irDeviceList(request);
        }

        ApiResponse<Map<String, Object>> speakerList() throws IOException {
            McuidRequest request = new McuidRequest(copyCommon());
            request.setMcuid(getSpeakerMcuid());
            return client.speakerList(request);
        }

        String createSpeakerRecord(String text) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("text", text)
                    .put("spd", "6")
                    .put("pit", "6")
                    .put("vol", "9")
                    .put("per", "7")
                    .json());
            ApiResponse<Map<String, Object>> response = client.speakerAdd(request);
            if (response != null && response.isOk()) {
                cachedSpeakerRecordId = Optional.ofNullable(extractFirstValue(response.getData(), "id"))
                        .orElseGet(() -> extractFirstValue(response.getData(), "recordid"));
            }
            if (cachedSpeakerRecordId == null) {
                ApiResponse<Map<String, Object>> listResp = speakerList();
                if (listResp != null) {
                    cachedSpeakerRecordId = extractFirstValue(listResp.getData(), "id");
                }
            }
            return cachedSpeakerRecordId;
        }

        ApiResponse<Map<String, Object>> speakerPlay(String recordId) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("id", recordId)
                    .json());
            return client.speakerPlay(request);
        }

        ApiResponse<Map<String, Object>> speakerDel(String recordId) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("ids", Collections.singletonList(recordId))
                    .json());
            ApiResponse<Map<String, Object>> response = client.speakerDel(request);
            if (response != null && response.isOk()) {
                cachedSpeakerRecordId = null;
            }
            return response;
        }

        ApiResponse<Map<String, Object>> speakerPlayText(String text) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("text", text)
                    .put("spd", "5")
                    .put("pit", "5")
                    .put("vol", "9")
                    .put("per", "7")
                    .json());
            return client.speakerPlayText(request);
        }

        ApiResponse<Map<String, Object>> speakerPlayLongText(String text) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("text", text)
                    .put("spd", "5")
                    .put("pit", "5")
                    .put("vol", "9")
                    .put("per", "7")
                    .json());
            return client.speakerPlayLongText(request);
        }

        ApiResponse<Map<String, Object>> speakerPlayFile(String fileId) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("id", fileId)
                    .json());
            return client.speakerPlayFile(request);
        }

        ApiResponse<Map<String, Object>> speakerPlayOnline(String url) throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatapoint(DatapointBuilder.create()
                    .put("url", url)
                    .json());
            return client.speakerPlayOnline(request);
        }

        ApiResponse<Map<String, Object>> setCountdownAlarm(String mcuid) throws IOException {
            DatapointRequest request = datapointRequest(mcuid);
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("sec", 600)
                    .put("alarm", "300,0")
                    .put("alarmtype", 0)
                    .json());
            return client.setCountdownAlarm(request);
        }

        ApiResponse<Map<String, Object>> speakerSdFile() throws IOException {
            DatapointRequest request = datapointRequest(getSpeakerMcuid());
            request.setDatatype(1);
            request.setDatapoint(DatapointBuilder.create()
                    .put("index", 1)
                    .put("loop", 1)
                    .json());
            return client.speakerSdFile(request);
        }

        ApiResponse<Map<String, Object>> gwDeviceList(String parentId) throws IOException {
            ParentIdRequest request = new ParentIdRequest(copyCommon());
            request.setParentid(parentId);
            return client.gwDeviceList(request);
        }

        List<Map<String, Object>> getDeviceMaps() {
            return deviceMaps;
        }

        String getSampleMcuid() {
            return sampleMcuid;
        }

        String getSampleMcuname() {
            return sampleMcuname;
        }

        String getSpeakerMcuid() {
            return speakerMcuid;
        }

        String getIrMcuid() {
            return irMcuid;
        }

        String getGatewayParentId() {
            return gatewayParentId;
        }

        String getCachedSpeakerRecordId() {
            return cachedSpeakerRecordId;
        }

        private CommonParams createCommonParams() {
            CommonParams common = ParamsFactory.of(APP_ID, DEVICE_ID, CLIENT_ID, ENDPOINT_OS);
            common.setClient_secret(CLIENT_SECRET);
            return common;
        }

        private CommonParams copyCommon() {
            CommonParams copy = ParamsFactory.of(APP_ID, DEVICE_ID, CLIENT_ID, ENDPOINT_OS);
            copy.setClient_secret(CLIENT_SECRET);
            copy.setContext(templateCommon.getContext());
            copy.setLocale(templateCommon.getLocale());
            copy.setClientVersion(templateCommon.getClientVersion());
            copy.setEndpointIP(templateCommon.getEndpointIP());
            copy.setDataTag(templateCommon.getDataTag());
            copy.setAreaId(templateCommon.getAreaId());
            return copy;
        }

        private Map<String, Object> login() throws IOException {
            LoginRequest request = new LoginRequest(copyCommon());
            request.setPhone(PHONE);
            request.setPassword(PASSWORD);
            request.setClient_secret(CLIENT_SECRET);
            ApiResponse<Map<String, Object>> response = client.login(request);
            if (response == null || !response.isOk()) {
                throw new AssumptionViolatedException("登錄失敗: " + (response == null ? "null" : response.getResultMsg()));
            }
            return Optional.ofNullable(response.getData()).orElse(Collections.emptyMap());
        }

        private void verifyTicket() throws IOException {
            String ticket = Optional.ofNullable(loginData.get("ticket")).map(Objects::toString)
                    .orElseThrow(() -> new AssumptionViolatedException("登錄未返回 ticket"));
            TicketVerifyRequest request = new TicketVerifyRequest(copyCommon());
            request.setTicket(ticket);
            ApiResponse<Map<String, Object>> response = client.verifyTicket(request);
            if (response == null || !response.isOk()) {
                throw new AssumptionViolatedException("驗票失敗: " + (response == null ? "null" : response.getResultMsg()));
            }
        }

        private void refreshDevices() throws IOException {
            ApiResponse<Map<String, Object>> response = client.queryDevices(new RequestBase(copyCommon()));
            if (response == null || !response.isOk()) {
                throw new AssumptionViolatedException("查詢設備失敗: " + (response == null ? "null" : response.getResultMsg()));
            }
            devicePayload = Optional.ofNullable(response.getData()).orElse(Collections.emptyMap());
            deviceMaps = collectDeviceMaps(devicePayload);
            sampleMcuid = extractFirstValue(devicePayload, "mcuid");
            sampleMcuname = extractFirstValue(devicePayload, "mcuname");
            speakerMcuid = findDeviceByKeyword("喇叭", "speaker");
            if (speakerMcuid == null) {
                speakerMcuid = sampleMcuid;
            }
            irMcuid = findDeviceByKeyword("紅外", "infrared", "ir");
            gatewayParentId = extractFirstValue(devicePayload, "parentid");
        }

        private DatapointRequest datapointRequest(String mcuid) {
            DatapointRequest request = new DatapointRequest(copyCommon());
            request.setMcuid(mcuid);
            return request;
        }

        private List<Map<String, Object>> singleChannelPayload() {
            Map<String, Object> channel = new java.util.HashMap<>();
            channel.put("index", 1);
            channel.put("status", 1);
            return Collections.singletonList(channel);
        }

        private List<Map<String, Object>> collectDeviceMaps(Object payload) {
            List<Map<String, Object>> result = new ArrayList<>();
            collectMapsRecursive(payload, result);
            return result;
        }

        @SuppressWarnings("unchecked")
        private void collectMapsRecursive(Object node, List<Map<String, Object>> bucket) {
            if (node instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) node;
                if (map.containsKey("mcuid")) {
                    bucket.add(map);
                }
                for (Object value : map.values()) {
                    collectMapsRecursive(value, bucket);
                }
            } else if (node instanceof Iterable) {
                for (Object item : (Iterable<?>) node) {
                    collectMapsRecursive(item, bucket);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private String extractFirstValue(Object node, String key) {
            if (node instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) node;
                Object value = map.get(key);
                if (value instanceof String) {
                    return (String) value;
                }
                for (Object child : map.values()) {
                    String result = extractFirstValue(child, key);
                    if (result != null) {
                        return result;
                    }
                }
            } else if (node instanceof Iterable) {
                for (Object item : (Iterable<?>) node) {
                    String result = extractFirstValue(item, key);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

        private String findDeviceByKeyword(String... keywords) {
            for (Map<String, Object> map : deviceMaps) {
                for (Object value : map.values()) {
                    if (value instanceof String) {
                        String str = ((String) value).toLowerCase();
                        for (String keyword : keywords) {
                            if (str.contains(keyword.toLowerCase())) {
                                return extractFirstValue(map, "mcuid");
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
}
