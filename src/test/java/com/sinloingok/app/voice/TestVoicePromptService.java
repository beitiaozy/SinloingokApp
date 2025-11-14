package com.sinloingok.app.voice;

import com.sinloingok.app.TestSinloingokApplication;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.verify;

public class TestVoicePromptService extends TestSinloingokApplication {

    @Autowired
    private VoicePromptService voicePromptService;

    @MockBean
    private SmyooVoiceGateway smyooVoiceGateway;

    @Test
    public void testGetPrompt() {
        Map<String, String> data = Collections.singletonMap("nickname", "阿豪");
        String msg = voicePromptService.getPrompt("B03", data);
        Assert.assertEquals("阿豪您好，歡迎光臨洗涞樂自助洗車。", msg);
    }

    @Test
    public void testPlayVoiceDelegatesToGateway() {
        String text = "迎光臨洗淶樂自助洗車";

        voicePromptService.playText(text);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(smyooVoiceGateway).playText(captor.capture());
        Assert.assertEquals(text, captor.getValue());
    }
}
