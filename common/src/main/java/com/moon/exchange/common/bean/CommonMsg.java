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
}
