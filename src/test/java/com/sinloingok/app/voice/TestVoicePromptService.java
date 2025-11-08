package com.sinloingok.app.voice;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.TestSinloingokApplication;
import com.sinloingok.app.constant.CommonParams;
import com.sinloingok.app.constant.ErrorCodes;
import com.sinloingok.app.dtos.smyoo.ApiResponse;
import com.sinloingok.app.dtos.smyoo.SmyooClient;
import com.sinloingok.app.dtos.smyoo.req.DatapointRequest;
import com.sinloingok.app.dtos.smyoo.req.LoginRequest;
import com.sinloingok.app.dtos.smyoo.req.RequestBase;
import com.sinloingok.app.dtos.smyoo.req.TicketVerifyRequest;
import com.sinloingok.app.service.VoicePromptService;
import com.sinloingok.app.util.smyoo.DatapointBuilder;
import com.sinloingok.app.util.smyoo.HttpTransport;
import com.sinloingok.app.util.smyoo.ParamsFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class TestVoicePromptService extends TestSinloingokApplication {

    @Autowired
    private VoicePromptService voicePromptService;

    @Test
    public void testGetPrompt() {
        Map<String, String> data = Collections.singletonMap("nickname", "阿豪");
        String msg = voicePromptService.getPrompt("B03", data);
        System.out.println(msg);
    }

    @Test
    public void testPlayVoice() throws IOException, InterruptedException {
        HttpTransport http = new HttpTransport();
        SmyooClient client = new SmyooClient(http);
//        东莞市洗涞乐科技有限公司
//        client_id：86922127
//
//        client_secret ：EF4DDE0AC00F44939BF6FCFCBC213EC1
//
//        deviceid: 82CA48CC5D2A4959BE1CFFE99974D924
        // 1) 公共参数
        CommonParams common = ParamsFactory.of(1314, "82CA48CC5D2A4959BE1CFFE99974D924", "86922127", 1);
        // 2) 登录
        LoginRequest login = new LoginRequest();
        login.setCommon(common);
        login.setPhone("18382051045");
        login.setPassword("Dxpbl1904");
        login.setClient_secret("EF4DDE0AC00F44939BF6FCFCBC213EC1"); // 与 client_id 配套
        ApiResponse<Map<String,Object>> loginR = client.login(login);
        System.out.println(JSONObject.toJSONString(loginR));
        if (!loginR.isOk()) throw new RuntimeException("login fail: " + loginR.getResultMsg());
//
//        // 3) 验票（将写入 Cookie: BpeSessionId）
//        TicketVerifyRequest tv = new TicketVerifyRequest();
//        tv.setCommon(common);
//        tv.setTicket((String)((Map<?,?>)loginR.getData()).get("ticket"));
//        ApiResponse<Map<String,Object>> tvR = client.verifyTicket(tv);
//        if (!tvR.isOk()) throw new RuntimeException("ticket verify fail");
//
//        // 4) 查询是否变更
//        ApiResponse<Map<String,Object>> sc = client.statusChanged(new RequestBase(common));
//        // 若 deviceupdatetime 变更，再调用 queryDevices
//        ApiResponse<Map<String,Object>> devices = client.queryDevices(new RequestBase(common));
//
//        // 5) 控制：打开多通道中的第1路
//        DatapointRequest ch = new DatapointRequest();
//        ch.setCommon(common);
//        ch.setMcuid("517A181B179C5A44E212834ED84389B5");
//        ch.setDatatype(1);
//        ch.setDatapoint(
//                DatapointBuilder.create()
//                        .put("index", 1)
//                        .put("status", 1) // 1开 0关
//                        .json()
//        );
//        ApiResponse setChResp = client.setChannelData(ch);
//        if (!setChResp.isOk()) {
//            if (setChResp.getResultCode() == ErrorCodes.SESSION_INVALID) {
//                // 处理：重新登录
//            }
//        }

        // 6) 云喇叭直接播放文字（≤45字）
        DatapointRequest say = new DatapointRequest();
        say.setCommon(common);
        say.setMcuid("957A1E4D718C3247B679859F292F9BD8");
        say.setDatapoint(
                DatapointBuilder.create()
                        .put("text", "歡迎光臨洗淶樂自助洗車")
                        .put("spd", "6")
                        .put("pit", "6")
                        .put("vol", "10")
                        .put("per", "7")
                        .json()
        );
        long sleep = 2000l;
        client.speakerPlayText(say);
        Thread.sleep(sleep);
        say.setDatapoint(
                DatapointBuilder.create()
                        .put("text", "清水已啟動")
                        .put("spd", "6")
                        .put("pit", "6")
                        .put("vol", "10")
                        .put("per", "8")
                        .json()
        );
        client.speakerPlayText(say);
        Thread.sleep(sleep);
        say.setDatapoint(
                DatapointBuilder.create()
                        .put("text", "清水已暫停")
                        .put("spd", "6")
                        .put("pit", "6")
                        .put("vol", "10")
                        .put("per", "9")
                        .json()
        );
        client.speakerPlayText(say);
        Thread.sleep(sleep);
        System.out.println("+");

        // 7) 倒计时闹钟（30分钟、15分钟、5分钟、到点提醒，闪灯方式）
//        DatapointRequest alarm = new DatapointRequest();
//        alarm.setCommon(common);
//        alarm.setMcuid("6A7EE4249F283C6A1EEE599CC3327232");
//        alarm.setDatatype(1);
//        alarm.setDatapoint(
//                DatapointBuilder.create()
//                        .put("sec", 10800)
//                        .put("alarm", "1800,900,300,0")
//                        .put("alarmtype", 1) // 0声音 1闪灯
//                        .json()
//        );
//        client.setCountdownAlarm(alarm);
    }

}
