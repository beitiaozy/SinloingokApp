package com.sinloingok.app.voice;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.TestSinloingokApplication;
import com.sinloingok.app.constant.CommonParams;
import com.sinloingok.app.dtos.smyoo.ApiResponse;
import com.sinloingok.app.dtos.smyoo.SmyooClient;
import com.sinloingok.app.dtos.smyoo.req.DatapointRequest;
import com.sinloingok.app.dtos.smyoo.req.LoginRequest;
import com.sinloingok.app.dtos.smyoo.req.RequestBase;
import com.sinloingok.app.dtos.smyoo.req.TicketVerifyRequest;
import com.sinloingok.app.util.smyoo.DatapointBuilder;
import com.sinloingok.app.util.smyoo.HttpTransport;
import com.sinloingok.app.util.smyoo.ParamsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmyooClientFlowTest extends TestSinloingokApplication {

    private static final String PLAYBACK_TEXT = "策杖只因與雪恥，橫戈原不為封侯";

    @Test
    public void testLoginVerifyDevicesAndPlayback() {
        StubHttpTransport transport = new StubHttpTransport();
        SmyooClient client = new SmyooClient(transport);

        CommonParams common = ParamsFactory.of(2024, "DEVICE-GATEWAY", "CLIENT-XYZ", 1);

        LoginRequest loginRequest = new LoginRequest(common);
        loginRequest.setPhone("13800000000");
        loginRequest.setPassword("password");
        loginRequest.setClient_secret("secret");

        ApiResponse<Map<String, Object>> loginResponse = client.login(loginRequest);
        Assert.assertTrue(loginResponse.isOk());
        Map<String, Object> loginData = loginResponse.getData();
        Assert.assertNotNull(loginData);
        String ticket = loginData.get("ticket").toString();
        String sessionId = loginData.get("sessionId").toString();
        Assert.assertNotNull(ticket);
        Assert.assertNotNull(sessionId);
        common.setTicket(ticket);
        common.setSessionId(sessionId);

        TicketVerifyRequest verifyRequest = new TicketVerifyRequest(common);
        verifyRequest.setTicket(ticket);
        ApiResponse<Map<String, Object>> verifyResponse = client.verifyTicket(verifyRequest);
        Assert.assertTrue(verifyResponse.isOk());
        Assert.assertNotNull(transport.getLastVerifyRequest());
        Assert.assertEquals(ticket, transport.getLastVerifyRequest().getTicket());

        ApiResponse<Map<String, Object>> devicesResponse = client.queryDevices(new RequestBase(common));
        Assert.assertTrue(devicesResponse.isOk());
        List<Map<String, Object>> devices = castDeviceList(devicesResponse.getData().get("devices"));
        Assert.assertNotNull(devices);
        Assert.assertFalse(devices.isEmpty());

        for (Map<String, Object> device : devices) {
            String mcuid = device.get("mcuid").toString();
            DatapointRequest say = new DatapointRequest(common);
            say.setMcuid(mcuid);
            say.setDatapoint(DatapointBuilder.create().put("text", PLAYBACK_TEXT).json());
            client.speakerPlayText(say);
        }

        List<DatapointRequest> playbackRequests = transport.getPlaybackRequests();
        Assert.assertEquals(devices.size(), playbackRequests.size());

        for (int i = 0; i < playbackRequests.size(); i++) {
            DatapointRequest request = playbackRequests.get(i);
            Map<String, Object> device = devices.get(i);
            Assert.assertSame(common, request.getCommon());
            Assert.assertEquals(device.get("mcuid"), request.getMcuid());
            JSONObject datapoint = JSONObject.parseObject(request.getDatapoint());
            Assert.assertEquals(PLAYBACK_TEXT, datapoint.getString("text"));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castDeviceList(Object devices) {
        if (devices instanceof List) {
            return (List<Map<String, Object>>) devices;
        }
        return new ArrayList<>();
    }

    private static class StubHttpTransport extends HttpTransport {

        private final List<DatapointRequest> playbackRequests = new ArrayList<>();
        private TicketVerifyRequest lastVerifyRequest;

        @Override
        public ApiResponse<Map<String, Object>> postForMap(String endpoint, Object payload) {
            if ("/open/login".equals(endpoint)) {
                Map<String, Object> data = new HashMap<>();
                data.put("ticket", "TICKET-20240101");
                data.put("sessionId", "SESSION-20240101");
                return ApiResponse.success(data);
            }
            if ("/open/ticket-verify".equals(endpoint) && payload instanceof TicketVerifyRequest) {
                lastVerifyRequest = (TicketVerifyRequest) payload;
                Map<String, Object> data = new HashMap<>();
                data.put("verified", Boolean.TRUE);
                return ApiResponse.success(data);
            }
            if ("/open/devices".equals(endpoint)) {
                Map<String, Object> data = new HashMap<>();
                data.put("devices", createDevices());
                return ApiResponse.success(data);
            }
            return ApiResponse.success(new HashMap<>());
        }

        @Override
        public <T> ApiResponse<T> postForObject(String endpoint, Object payload, Class<T> type) {
            if ("/open/speaker-play-text".equals(endpoint) && payload instanceof DatapointRequest) {
                playbackRequests.add((DatapointRequest) payload);
                return ApiResponse.success(null);
            }
            return ApiResponse.success(null);
        }

        private List<Map<String, Object>> createDevices() {
            List<Map<String, Object>> devices = new ArrayList<>();
            devices.add(createDevice("DEV-1", "MCU-1001"));
            devices.add(createDevice("DEV-2", "MCU-1002"));
            devices.add(createDevice("DEV-3", "MCU-1003"));
            return devices;
        }

        private Map<String, Object> createDevice(String deviceId, String mcuid) {
            Map<String, Object> device = new LinkedHashMap<>();
            device.put("deviceId", deviceId);
            device.put("mcuid", mcuid);
            device.put("name", "Device-" + deviceId);
            return device;
        }

        public List<DatapointRequest> getPlaybackRequests() {
            return playbackRequests;
        }

        public TicketVerifyRequest getLastVerifyRequest() {
            return lastVerifyRequest;
        }
    }
}

