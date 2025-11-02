package com.sinloingok.app.util.ns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NettyChannelRegistry {

    private final ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    public Channel register(String onlyCode, Channel channel) {
        return channelMap.put(onlyCode, channel);
    }

    public String remove(Channel channel) {
        for (Map.Entry<String, Channel> entry : channelMap.entrySet()) {
            if (entry.getValue().equals(channel)) {
                String key = entry.getKey();
                channelMap.remove(key);
                return key;
            }
        }
        return null;
    }

    public String getOnlyCode(Channel channel) {
        for (Map.Entry<String, Channel> entry : channelMap.entrySet()) {
            if (entry.getValue().equals(channel)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Channel getChannel(String onlyCode) {
        return channelMap.get(onlyCode);
    }

    public String sendMsg(String onlyCode, String hexMessage) {
        Channel channel = channelMap.get(onlyCode);
        if (channel == null) {
            log.debug("trace={} phase=io step=send msg=not_found device={} hex={}", MDC.get("trace"), onlyCode, hexMessage);
            return "not_found";
        }
        try {
            byte[] bytes = SignalPayloadUtils.hexStringToBytes(hexMessage);
            final int chunkSize = 1024;
            int off = 0;
            while (off < bytes.length) {
                int len = Math.min(chunkSize, bytes.length - off);
                ByteBuf buffer = Unpooled.wrappedBuffer(bytes, off, len);
                channel.write(buffer);
                off += len;
            }
            channel.flush();
            return "success";
        } catch (Exception e) {
            log.error("trace={} phase=io step=send_exception device={}", MDC.get("trace"), onlyCode, e);
            return "error";
        }
    }
}
