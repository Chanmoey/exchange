package com.moon.exchange.common.bean;

import lombok.*;

import java.io.Serializable;

/**
 * TCP传递的报文格式
 *
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommonMsg implements Serializable {

    /**
     * 包头[包体长度int|校验和byte|srt short|dst short|消息类型 short|消息状态 byte|包编号 long]
     * 包体[数据 byte[]]
     */
    private int bodyLength;

    private byte checksum;

    private short msgSrc;

    private short msgDst;

    private short msgType;

    private byte status;

    private long msgNo;

    @ToString.Exclude
    private byte[] body;

    /**
     * 校验该包是否合法
     */
    private boolean isLegal;

    private short errCode;

    private long timestamp;

    /**
     * 创建CommonMsg
     * @param data 字节数据
     * @param checksum 校验和
     * @param msgSrc 源
     * @param msgDst 目的
     * @param msgType 消息类型
     * @param status 消息状态
     * @param packetNo 消息编号
     * @return CommonMsg
     */
    public static CommonMsg createCommonMsg(byte[] data, byte checksum, short msgSrc, short msgDst,
                                     short msgType, byte status, long packetNo) {
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(data.length);
        msg.setChecksum(checksum);
        msg.setMsgSrc(msgSrc);
        msg.setMsgDst(msgDst);
        msg.setMsgType(msgType);
        msg.setStatus(status);
        msg.setMsgNo(packetNo);
        msg.setBody(data);
        return msg;
    }
}
