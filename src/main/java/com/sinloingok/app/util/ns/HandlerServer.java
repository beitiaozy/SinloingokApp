package com.sinloingok.app.util.ns;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.order.ChannelLockManager;
import com.sinloingok.app.service.order.CommandExecutor;
import com.sinloingok.app.util.StringValidationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class HandlerServer extends ChannelInboundHandlerAdapter {

    private static final String SWITCH_SIGNAL_PREFIX = "eeffc001";
    private static final String HEARTBEAT_PREFIX     = "a50012594a";
    private static final String RELAY_FEEDBACK       = "4f4b21";

    private static final byte[] PONG_BYTES = "PONG".getBytes(StandardCharsets.US_ASCII);

    private final NettyChannelRegistry channelRegistry;
    private final HeartbeatProcessor heartbeatProcessor;
    private final PulseSignalProcessor pulseSignalProcessor;
    private final ChannelLockManager channelLockManager;

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
                pulseSignalProcessor.process(ctx, hexData, this::getCommandExecutor);
            } else if (hexData.startsWith(HEARTBEAT_PREFIX) && hexData.length() >= 10) {
                String onlyCode = heartbeatProcessor.process(ctx, hexData, this::getNetSiteService);
                if (onlyCode != null) {
                    safeWriteAndFlush(ctx, Unpooled.wrappedBuffer(PONG_BYTES));
                }
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

    private void processRelayFeedback(ChannelHandlerContext ctx, String hexData) {
        String onlyCode = channelRegistry.getOnlyCode(ctx.channel());
        log.info("trace={} phase=relay step=feedback onlyCode={} hexData={}", MDC.get("trace"), onlyCode, hexData);
        if (onlyCode != null) {
            channelLockManager.onRelayFeedback(onlyCode);
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

    private void removeChannel(Channel channel) {
        String removedKey = channelRegistry.remove(channel);
        if (removedKey != null) {
            log.info("trace={} phase=netty step=channel_removed onlyCode={} channelId={}",
                    MDC.get("trace"), removedKey, channel.id().asLongText());
        } else {
            log.info("trace={} phase=netty step=channel_removed_unknown channelId={}",
                    MDC.get("trace"), channel.id().asLongText());
        }
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
}
