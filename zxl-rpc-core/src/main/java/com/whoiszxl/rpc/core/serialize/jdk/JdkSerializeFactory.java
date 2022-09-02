package com.whoiszxl.rpc.core.serialize.jdk;

import com.whoiszxl.rpc.core.serialize.SerializeFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JdkSerializeFactory implements SerializeFactory {

    @Override
    public <T> byte[] serialize(T t) {
        byte[] data;

        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(t);
            oos.flush();
            oos.close();
            data = bos.toByteArray();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try{
            ObjectInputStream inputStream = new ObjectInputStream(bis);
            Object result = inputStream.readObject();
            return ((T) result);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
