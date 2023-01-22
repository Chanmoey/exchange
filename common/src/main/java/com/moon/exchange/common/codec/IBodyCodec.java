package com.moon.exchange.common.codec;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public interface IBodyCodec {

    /**
     * 将Java对象序列化长字节数组
     */
    <T> byte[] serialize(T obj) throws Exception;

    <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception;
}
