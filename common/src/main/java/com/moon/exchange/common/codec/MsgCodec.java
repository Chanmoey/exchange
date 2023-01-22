package com.moon.exchange.common.codec;

import com.moon.exchange.common.bean.CommonMsg;
import io.vertx.core.buffer.Buffer;


/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public class MsgCodec implements IMsgCodec {
    @Override
    public Buffer encodeToBuffer(CommonMsg msg) {
        return Buffer.buffer()
                .appendInt(msg.getBodyLength())
                .appendByte(msg.getChecksum())
                .appendShort(msg.getMsgSrc())
                .appendShort(msg.getMsgDst())
                .appendShort(msg.getMsgType())
                .appendByte(msg.getStatus())
                .appendLong(msg.getMsgNo())
                .appendBytes(msg.getBody());
    }

    @Override
    public CommonMsg decodeFromBuffer(Buffer buffer) {
        int bodyLength = buffer.getInt(0);
        byte checksum = buffer.getByte(4);
        short msgSrc = buffer.getShort(5);
        short msgDst = buffer.getShort(7);
        short msgType = buffer.getShort(9);
        byte mstStats = buffer.getByte(11);
        long packetNo = buffer.getLong(12);
        byte[] bodyBytes = buffer.getBytes(20, 20 + bodyLength);

        return CommonMsg.createCommonMsg(bodyBytes, checksum, msgSrc, msgDst,
                msgType, mstStats, packetNo);
    }
}
