package com.sinloingok.app.util.ns;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.order.CommandExecutor;
import com.sinloingok.app.util.StringValidationUtil;
import com.sinloingok.app.util.Trace;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Slf4j
@Component
@ChannelHandler.Sharable
public class HandlerServer extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private static Map<String, String> BIT_REVERSE_TABLE;
    static {
        Map<String, String> bitReverseTable = new LinkedHashMap<>();
        bitReverseTable.put("00", "00");
        bitReverseTable.put("01", "80");
        bitReverseTable.put("02", "40");
        bitReverseTable.put("04", "20");
        bitReverseTable.put("08", "10");
        bitReverseTable.put("10", "08");
        bitReverseTable.put("20", "04");
        bitReverseTable.put("40", "02");
        bitReverseTable.put("80", "01");
        BIT_REVERSE_TABLE = Collections.unmodifiableMap(bitReverseTable);
    }

    // 协议标识常量
    private static final String SWITCH_SIGNAL_PREFIX = "eeffc001";
    private static final String HEARTBEAT_PREFIX     = "a50012594a";
    private static final String RELAY_FEEDBACK       = "4f4b21";

    private static final byte[] PONG_BYTES = "PONG".getBytes(StandardCharsets.US_ASCII);

    public static int convert(String input) {
        try {
            int intValue = Integer.parseInt(input, 16);
            int result = (int) (Math.log(intValue) / Math.log(2)) + 1;
            return result < 0 ? 0 : result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("输入不是有效数字: " + input);
        }
    }

    private volatile NetSiteService netSiteService;
    private volatile CommandExecutor executor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ByteBuf)) return;

        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            String hexData = ByteBufUtil.hexDump(byteBuf);
            String channelId = ctx.channel().id().asLongText();
            processMessage(ctx, hexData, channelId);
        } catch (Exception e) {
            log.error("trace={} phase=ingress step=error msg=parse_exception channelId={}",
                    MDC.get("trace"), ctx.channel().id().asLongText(), e);
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private void processMessage(ChannelHandlerContext ctx, String hexData, String channelId) {
        if (hexData == null || hexData.isEmpty()) return;

        try {
            if ((hexData.startsWith(SWITCH_SIGNAL_PREFIX) && hexData.length() >= 18)
                    || StringValidationUtil.isValidFormat(hexData)) {
                processSwitchSignal(ctx, hexData);
            } else if (hexData.startsWith(HEARTBEAT_PREFIX) && hexData.length() >= 10) {
                processHeartbeat(ctx, hexData);
            } else if (RELAY_FEEDBACK.equalsIgnoreCase(hexData)) {
                processRelayFeedback(ctx, hexData);
            } else {
                log.debug("trace={} phase=ingress step=filter msg=unknown_hex len={} hex={}",
                        MDC.get("trace"), hexData.length(), hexData);
            }
        } catch (Exception ex) {
            log.error("trace={} phase=ingress step=exception hex={}", MDC.get("trace"), hexData, ex);
        }
    }

    /** 处理开关量信号 */
    private void processSwitchSignal(ChannelHandlerContext ctx, String hexData) {
        if (hexData.length() < 16) {
            log.warn("trace={} phase=ingress step=validate msg=switch_hex_too_short len={} hex={}",
                    MDC.get("trace"), hexData.length(), hexData);
            return;
        }

        String beginSignal;
        String endSignal;
        String onlyCode = getOnlyCodeByChannel(ctx.channel());

        if (onlyCode != null && NetSiteCache.pmByOnlyCode(onlyCode) != null) {
            // PM端：不做位翻转
            beginSignal = safeSub(hexData, 12, 14);
            endSignal   = safeSub(hexData, 14, 16);
        } else {
            beginSignal = reverseBitsLookup(safeSub(hexData, 12, 14));
            endSignal   = reverseBitsLookup(safeSub(hexData, 14, 16));
        }

        if (onlyCode == null) {
            log.warn("trace={} phase=ingress step=bind msg=onlyCode_not_found hex={} begin={} end={}",
                    MDC.get("trace"), hexData, beginSignal, endSignal);
            return;
        }

        handlePulseSignal(onlyCode, beginSignal, endSignal);
    }

    public static String reverseBitsLookup(String hexByte) {
        if (hexByte == null || hexByte.length() != 2) return "00";
        return BIT_REVERSE_TABLE.getOrDefault(hexByte.toUpperCase(), "00");
    }

    /** 处理心跳信号 */
    private void processHeartbeat(ChannelHandlerContext ctx, String hexData) {
        if (hexData.length() < 34) {
            log.warn("trace={} phase=heartbeat step=validate msg=heartbeat_hex_too_short len={} hex={}",
                    MDC.get("trace"), hexData.length(), hexData);
            return;
        }
        String onlyCode = extractOnlyCodeFromHeartbeat(hexData);
        onlyCode = onlyCodeChange(onlyCode);

        Channel oldChannel = CHANNEL_MAP.put(onlyCode, ctx.channel());
        logChannelUpdate(onlyCode, ctx.channel(), oldChannel);

        sendMsg(onlyCode, hexData);
        safeWriteAndFlush(ctx, Unpooled.wrappedBuffer(PONG_BYTES));

        handleHeartbeat(onlyCode, hexData);
    }

    private String onlyCodeChange(String onlyCode){
        if("78EE4C6C8DFC".equals(onlyCode)){
            onlyCode = "0090D500242A";
        }
        if("9015060247C4".equals(onlyCode)){
            onlyCode = "0090D5000F10";
        }
        if("90150604FDDC".equals(onlyCode)){
            onlyCode = "0090D500245B";
        }
        return onlyCode;
    }

    private String extractOnlyCodeFromHeartbeat(String hexData) {
        String dataPart = hexData.substring(10, 34);
        return IntStream.range(0, 12)
                .mapToObj(i -> dataPart.substring(i * 2, i * 2 + 2))
                .map(this::hexToAscii)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private void processRelayFeedback(ChannelHandlerContext ctx, String hexData) {
        String onlyCode = getOnlyCodeByChannel(ctx.channel());
        log.info("trace={} phase=relay step=feedback onlyCode={} hexData={}", MDC.get("trace"), onlyCode, hexData);
    }

    /**
     * 处理脉冲信号：统一生成 traceId、打印首条 FLOW_START、贯穿执行
     */
    private void handlePulseSignal(String onlyCode, String beginSignal, String endSignal) {
        long t0 = System.nanoTime();

        DeviceControl.Action action = "00".equals(beginSignal) ? DeviceControl.Action.CLOSE : DeviceControl.Action.OPEN;
        int b = safeConvert(beginSignal);
        int e = safeConvert(endSignal);
        int channelNo = b + e;

        String funcCode = channelNo > 0 ? SignalTopology.getFunctionCode(channelNo) : "NA";
        String funcName = channelNo > 0 ? SignalTopology.getFunctionName(channelNo) : "NA";

        String traceId = Trace.newId();
        Trace.bind(traceId, onlyCode, channelNo, action.name(), funcCode, funcName);

        try {
            boolean runnable = (NetSiteCache.wscByOnlyCode(onlyCode) != null
                    && NetSiteStatus.USING.equals(NetSiteCache.wscByOnlyCode(onlyCode).getStatus()))
                    || (NetSiteCache.pmByOnlyCode(onlyCode) != null);

            // ——【首条头牌日志】——
            log.info("trace={} phase=FLOW_START funcName={} funcCode={} action={} channel={} onlyCode={} begin={} end={}",
                    traceId, funcName, funcCode, action.name(), channelNo, onlyCode, beginSignal, endSignal);

            if (!runnable) {
                log.info("trace={} phase=gateway step=guard msg=site_not_running", traceId);
                return;
            }

            log.info("trace={} phase=gateway step=dispatch to=CommandExecutor", traceId);
            CommandExecutor executor = getCommandExecutor();
            boolean ok = executor.executeWithTrace(onlyCode, channelNo, action, traceId);
            long costMs = (System.nanoTime() - t0) / 1_000_000;
            log.info("trace={} phase=gateway step=done result={} costMs={}", traceId, ok ? "OK" : "FAIL", costMs);
        } catch (Exception e1) {
            long costMs = (System.nanoTime() - t0) / 1_000_000;
            log.error("trace={} phase=gateway step=exception costMs={}", traceId, costMs, e1);
        } finally {
            Trace.clear();
        }
    }

    private int safeConvert(String hex) {
        try {
            return convert(hex);
        } catch (Exception e) {
            log.warn("trace={} phase=gateway step=convert_fail hex={}", MDC.get("trace"), hex);
            return 0;
        }
    }

    private void handleHeartbeat(String onlyCode, String hexData) {
        try {
            NetSiteService service = getNetSiteService();
            service.registerOrRefreshNetSite(onlyCode);
        } catch (Exception e) {
            log.error("trace={} phase=heartbeat step=handle_exception onlyCode={}", MDC.get("trace"), onlyCode, e);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                safeWriteAndFlush(ctx, Unpooled.wrappedBuffer(PONG_BYTES));
                log.debug("trace={} phase=idle step=writer_idle pong_to={}", MDC.get("trace"), ctx.channel().remoteAddress());
            } else if (e.state() == IdleState.READER_IDLE) {
                log.warn("trace={} phase=idle step=reader_idle close={}", MDC.get("trace"), ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("trace={} phase=netty step=handler_added channelId={}", MDC.get("trace"), ctx.channel().id().asLongText());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String clientIp = String.valueOf(ctx.channel().remoteAddress());
        log.info("trace={} phase=netty step=channel_active remote={}", MDC.get("trace"), clientIp);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("trace={} phase=netty step=handler_removed channelId={}", MDC.get("trace"), ctx.channel().id().asLongText());
        removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("trace={} phase=netty step=exception channelId={}", MDC.get("trace"), ctx.channel().id().asLongText(), cause);
        removeChannel(ctx.channel());
        ctx.close();
    }

    public static String sendMsg(String onlyCode, String hexMessage) {
        Channel channel = CHANNEL_MAP.get(onlyCode);
        if (channel == null) {
            log.debug("trace={} phase=io step=send msg=not_found device={} hex={}", MDC.get("trace"), onlyCode, hexMessage);
            return "not_found";
        }
        try {
            byte[] bytes = hexStringToBytes(hexMessage);
            final int CHUNK = 1024;
            int off = 0;
            while (off < bytes.length) {
                int len = Math.min(CHUNK, bytes.length - off);
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

    private void removeChannel(Channel channel) {
        String removedKey = CHANNEL_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(channel))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (removedKey != null) {
            log.info("trace={} phase=netty step=channel_removed onlyCode={} channelId={}",
                    MDC.get("trace"), removedKey, channel.id().asLongText());
            CHANNEL_MAP.remove(removedKey);
        } else {
            log.info("trace={} phase=netty step=channel_removed_unknown channelId={}",
                    MDC.get("trace"), channel.id().asLongText());
        }
    }

    private String getOnlyCodeByChannel(Channel channel) {
        return CHANNEL_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(channel))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void logChannelUpdate(String onlyCode, Channel newChannel, Channel oldChannel) {
        if (oldChannel == null) {
            log.info("trace={} phase=heartbeat step=register onlyCode={} channelId={}",
                    MDC.get("trace"), onlyCode, newChannel.id().asLongText());
        } else if (!oldChannel.id().equals(newChannel.id())) {
            log.warn("trace={} phase=heartbeat step=replace onlyCode={} oldChannelId={} newChannelId={}",
                    MDC.get("trace"), onlyCode, oldChannel.id().asLongText(), newChannel.id().asLongText());
        } else {
            log.debug("trace={} phase=heartbeat step=refresh onlyCode={} channelId={}",
                    MDC.get("trace"), onlyCode, newChannel.id().asLongText());
        }
    }

    private static byte[] hexStringToBytes(String hexString) {
        int length = hexString.length();
        if ((length & 1) == 1) {
            throw new IllegalArgumentException("hex 长度必须为偶数: " + length);
        }
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int hi = Character.digit(hexString.charAt(i), 16);
            int lo = Character.digit(hexString.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("非法hex字符: " + hexString.substring(i, i + 2));
            }
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }

    private String hexToAscii(String hex) {
        int decimal = Integer.parseInt(hex, 16);
        return String.valueOf((char) decimal);
    }

    private NetSiteService getNetSiteService() {
        if (netSiteService == null) {
            synchronized (this) {
                if (netSiteService == null) {
                    netSiteService = SBeanUtils.getBean(NetSiteService.class);
                }
            }
        }
        return netSiteService;
    }

    private CommandExecutor getCommandExecutor() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = SBeanUtils.getBean(CommandExecutor.class);
                }
            }
        }
        return executor;
    }

    private void safeWriteAndFlush(ChannelHandlerContext ctx, Object msg) {
        try {
            ctx.writeAndFlush(msg).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warn("trace={} phase=io step=write_fail cause={}",
                            MDC.get("trace"), f.cause() != null ? f.cause().toString() : "unknown");
                }
            });
        } catch (Exception e) {
            log.warn("trace={} phase=io step=write_exception ex={}", MDC.get("trace"), e.toString());
        }
    }

    private String safeSub(String s, int start, int end) {
        if (s == null) return "";
        if (start < 0) start = 0;
        if (end > s.length()) end = s.length();
        if (start >= end) return "";
        return s.substring(start, end);
    }
}
