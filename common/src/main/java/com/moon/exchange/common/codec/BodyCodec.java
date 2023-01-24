package com.moon.exchange.common.codec;


import com.alipay.remoting.serialization.SerializerManager;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public class BodyCodec implements IBodyCodec {

    /**
     * 使用Hessian2算法进行序列号
     */
    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2)
                .serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2)
                .deserialize(bytes, clazz.getName());
    }

}
