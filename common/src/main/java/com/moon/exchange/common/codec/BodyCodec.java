package com.moon.exchange.common.codec;


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
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(os);
        ho.writeObject(obj);
        ho.getBytesOutputStream().flush();
        ho.completeMessage();
        ho.close();
        return os.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Hessian2Input hi = new Hessian2Input(is);
        return (T) hi.readObject(clazz);
    }

}
