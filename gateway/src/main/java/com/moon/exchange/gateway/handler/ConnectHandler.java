package com.moon.exchange.gateway.handler;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.gateway.config.GatewayConfig;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Log4j2
@RequiredArgsConstructor
public class ConnectHandler implements Handler<NetSocket> {

    @NonNull
    private GatewayConfig config;

    /**
     * 包体长4 校验和1 src2 dst2 消息类型2 消息状态1 包编号8
     */
    private static final int PACKET_HEADER_LENGTH =
            4 + 1 + 2 + 2 + 2 + 1 + 8;

    @Override
    public void handle(NetSocket socket) {

        IMsgHandler msgHandler = new MsgHandler(config.getBodyCodec());

        msgHandler.onConnect(socket);

        // 设置报文解析器parser，开始时先读20个字节的头信息
        final RecordParser parser = RecordParser.newFixed(PACKET_HEADER_LENGTH);
        parser.setOutput(new Handler<>() {

            //包体长
            int bodyLength = -1;
            byte checksum = -1;
            short msgSrc = -1;
            short msgDst = -1;
            short msgType = -1;
            byte mstStats = -1;
            long packetNo = -1;

            @Override
            public void handle(Buffer buffer) {

                if (bodyLength == -1) {
                    //读包头
                    bodyLength = buffer.getInt(0);
                    checksum = buffer.getByte(4);
                    msgSrc = buffer.getShort(5);
                    msgDst = buffer.getShort(7);
                    msgType = buffer.getShort(9);
                    mstStats = buffer.getByte(11);
                    packetNo = buffer.getLong(12);
                    // 设置要往下读取的长度（包体）
                    parser.fixedSizeMode(bodyLength);
                } else {
                    // 读取包体正文
                    byte[] bodyBytes = buffer.getBytes();

                    if (checksum != config.getCheckSum().getCheckSum(bodyBytes)) {
                        // 传输错误，抛弃该包
                        log.error("illegal byte body exist from client: {}", socket.remoteAddress());
                        return;
                    }

                    // 包是不是真的发我们这个网关的
                    if (msgDst != config.getId()) {
                        log.error("recv error msgDst dst: {} from client: {}",
                                msgDst, socket.remoteAddress());
                        return;
                    }

                    // 组装对象
                    CommonMsg msg = new CommonMsg();
                    msg.setBodyLength(bodyLength);
                    msg.setChecksum(checksum);
                    msg.setMsgSrc(msgSrc);
                    msg.setMsgDst(msgDst);
                    msg.setMsgType(msgType);
                    msg.setStatus(mstStats);
                    msg.setMsgNo(packetNo);
                    msg.setBody(bodyBytes);
                    msg.setTimestamp(System.currentTimeMillis());

                    msgHandler.onCounterData(msg);

                    // 恢复现场
                    bodyLength = -1;
                    checksum = -1;
                    msgSrc = -1;
                    msgDst = -1;
                    msgType = -1;
                    mstStats = -1;
                    packetNo = -1;
                    parser.fixedSizeMode(PACKET_HEADER_LENGTH);
                }
            }
        });
        // 将解析器与socket关联
        socket.handler(parser);

        // 设置异常处理器
        socket.closeHandler(close -> {
            msgHandler.onDisConnect(socket);
        });

        socket.exceptionHandler(e -> {
            msgHandler.onException(socket, e);
            socket.close();
        });
    }
}
