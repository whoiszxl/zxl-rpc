package com.whoiszxl.rpc.core.proxy.jdk;

import com.whoiszxl.rpc.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * jdk代理工厂
 */
public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) throws Throwable {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new JDKClientInvocationHandler(clazz));
    }
}
