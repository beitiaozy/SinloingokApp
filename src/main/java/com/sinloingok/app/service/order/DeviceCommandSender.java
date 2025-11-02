package com.sinloingok.app.service.order;

import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.util.ns.NettyChannelRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 硬件执行器：对设备下发 OPEN/CLOSE/READ 指令
 * - 支持通道：1..16；0 号为“全开/全关/读”
 * - 指令表常量化、不可变
 * - 入参与存在性校验、详实日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCommandSender {

    private final NettyChannelRegistry channelRegistry;


    /** 统一入口：根据动作与通道下发 */
    public SendResult send(String onlyCode, int channel, DeviceControl.Action action) {
        if (StringUtils.isBlank(onlyCode)) {
            log.warn("发送失败：onlyCode 为空, action={}, channel={}", action, channel);
            return SendResult.failure(SendStatus.VALIDATION_FAILED, "onlyCode_blank");
        }
        if (channel < 0 || channel > 16) {
            log.warn("发送失败：非法通道 channel={}, 允许范围 0..16（0=全局）", channel);
            return SendResult.failure(SendStatus.VALIDATION_FAILED, "channel_out_of_range");
        }
        final String msg = resolveCommand(action, channel);
        if (msg == null) {
            log.warn("发送失败：未配置的命令 action={}, channel={}", action, channel);
            return SendResult.failure(SendStatus.VALIDATION_FAILED, "command_not_configured");
        }
        try {
            String sendResult = channelRegistry.sendMsg(onlyCode, msg);
            if ("success".equalsIgnoreCase(sendResult)) {
                log.info("下发成功 -> onlyCode={}, action={}, channel={}, raw={}", onlyCode, action, channel, msg);
                return SendResult.success();
            }
            if ("not_found".equalsIgnoreCase(sendResult)) {
                log.warn("下发失败（通道缺失）-> onlyCode={} action={} channel={} raw={}", onlyCode, action, channel, msg);
                return SendResult.failure(SendStatus.NOT_FOUND, "channel_not_found");
            }
            if ("error".equalsIgnoreCase(sendResult)) {
                log.error("下发失败（写入异常）-> onlyCode={} action={} channel={} raw={}", onlyCode, action, channel, msg);
                return SendResult.failure(SendStatus.ERROR, "channel_write_error");
            }
            log.warn("下发失败（未知返回）-> onlyCode={} action={} channel={} raw={} result={}",
                    onlyCode, action, channel, msg, sendResult);
            return SendResult.failure(SendStatus.UNKNOWN, "unknown_result:" + sendResult);
        } catch (Exception e) {
            log.error("下发异常 -> onlyCode={}, action={}, channel={}, raw={}", onlyCode, action, channel, msg, e);
            return SendResult.failure(SendStatus.ERROR, e.getClass().getSimpleName());
        }
    }

    /** 便捷方法 */
    public SendResult open(String onlyCode, int channel)  { return send(onlyCode, channel, DeviceControl.Action.OPEN); }
    public SendResult close(String onlyCode, int channel) { return send(onlyCode, channel, DeviceControl.Action.CLOSE); }
    public SendResult readAll(String onlyCode)            { return send(onlyCode, 0,       DeviceControl.Action.READ);  }

    public enum SendStatus {
        SUCCESS,
        VALIDATION_FAILED,
        NOT_FOUND,
        ERROR,
        UNKNOWN
    }

    public static final class SendResult {
        private final SendStatus status;
        private final String detail;

        private SendResult(SendStatus status, String detail) {
            this.status = status;
            this.detail = detail;
        }

        public static SendResult success() {
            return new SendResult(SendStatus.SUCCESS, "OK");
        }

        public static SendResult failure(SendStatus status, String detail) {
            return new SendResult(status, detail);
        }

        public boolean isSuccess() {
            return SendStatus.SUCCESS.equals(status);
        }

        public boolean isOffline() {
            return SendStatus.NOT_FOUND.equals(status);
        }

        public SendStatus getStatus() {
            return status;
        }

        public String getDetail() {
            return detail;
        }
    }

    /** 通过动作与通道解析指令原文（不可变 Map，线程安全） */
    private String resolveCommand(DeviceControl.Action action, int channel) {
        switch (action) {
            case OPEN:  return OPEN_MAP.get(channel);
            case CLOSE: return CLOSE_MAP.get(channel);
            case READ:  return READ_MAP.get(channel);
            default:    return null;
        }
    }

    // ===================== 指令表（常量） =====================

    /** OPEN 指令（1..16；0=全开） */
    private static final Map<Integer, String> OPEN_MAP;
    /** CLOSE 指令（1..16；0=全关） */
    private static final Map<Integer, String> CLOSE_MAP;
    /** READ 指令（仅 0=读全部） */
    private static final Map<Integer, String> READ_MAP;

    static {
        Map<Integer, String> open = new HashMap<>();
        Map<Integer, String> close = new HashMap<>();
        Map<Integer, String> read = new HashMap<>();

        // ------------- 明细通道（1..16） -------------
        open.put( 1, "CCDDA10100010001A448"); close.put( 1, "CCDDA10100000001A346");
        open.put( 2, "CCDDA10100020002A64C"); close.put( 2, "CCDDA10100000002A448");
        open.put( 3, "CCDDA10100040004AA54"); close.put( 3, "CCDDA10100000004A64C");
        open.put( 4, "CCDDA10100080008B264"); close.put( 4, "CCDDA10100000008AA54");

        open.put( 5, "CCDDA10100100010C284"); close.put( 5, "CCDDA10100000010B264");
        open.put( 6, "CCDDA10100200020E2C4"); close.put( 6, "CCDDA10100000020C284");
        open.put( 7, "CCDDA101004000402244"); close.put( 7, "CCDDA10100000040E2C4");
        open.put( 8, "CCDDA10100800080A244"); close.put( 8, "CCDDA101000000802244");

        open.put( 9, "CCDDA10101000100A448"); close.put( 9, "CCDDA10100000100A346");
        open.put(10, "CCDDA10102000200A64C"); close.put(10, "CCDDA10100000200A448");
        open.put(11, "CCDDA10104000400AA54"); close.put(11, "CCDDA10100000400A64C");
        open.put(12, "CCDDA10108000800B264"); close.put(12, "CCDDA10100000800AA54");

        open.put(13, "CCDDA10110001000C284"); close.put(13, "CCDDA10100001000B264");
        open.put(14, "CCDDA10120002000E2C4"); close.put(14, "CCDDA10100002000C284");
        open.put(15, "CCDDA101400040002244"); close.put(15, "CCDDA10100004000E2C4");
        open.put(16, "CCDDA10180008000A244"); close.put(16, "CCDDA101000080002244");

        // ------------- 全局通道（0） -------------
        open.put( 0, "CCDDA101FFFFFFFF9E3C"); // 全开
        close.put(0, "CCDDA1010000FFFFA040"); // 全关
        read.put( 0, "CCDDC00100000DCE9C");  // 全读

        OPEN_MAP  = Collections.unmodifiableMap(open);
        CLOSE_MAP = Collections.unmodifiableMap(close);
        READ_MAP  = Collections.unmodifiableMap(read);
    }
}
