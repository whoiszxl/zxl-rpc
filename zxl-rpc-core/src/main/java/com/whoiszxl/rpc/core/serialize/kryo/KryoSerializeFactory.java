package com.whoiszxl.rpc.core.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.whoiszxl.rpc.core.serialize.SerializeFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializeFactory implements SerializeFactory {

    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return new Kryo();
        }
    };

    @Override
    public <T> byte[] serialize(T t) {
        Output output = null;

        try{
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output, t);
            return output.toBytes();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if(output != null) {
                output.close();
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Input input = null;
        try {
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            input = new Input(byteArrayInputStream);
            return (T) kryo.readClassAndObject(input);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if(input != null) {
                input.close();
            }
        }
    }
}
