package com.whoiszxl.rpc.core.proxy.jdk;

import com.whoiszxl.rpc.core.client.RpcReferenceWrapper;
import com.whoiszxl.rpc.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * jdk代理工厂
 */
public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(),
                new Class[]{rpcReferenceWrapper.getAimClass()},
                new JDKClientInvocationHandler(rpcReferenceWrapper));
    }
}
