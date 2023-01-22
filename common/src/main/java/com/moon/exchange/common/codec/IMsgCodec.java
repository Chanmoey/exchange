package com.moon.exchange.common.codec;

import com.moon.exchange.common.bean.CommonMsg;
import io.vertx.core.buffer.Buffer;


/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public interface IMsgCodec {

    /**
     * 将CommonMsg转成TCP流
     */
    Buffer encodeToBuffer(CommonMsg msg);

    /**
     * 将TCP流转成CommonMsg
     */
    CommonMsg decodeFromBuffer(Buffer buffer);
}
