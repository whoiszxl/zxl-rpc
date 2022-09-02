package com.whoiszxl.rpc.core.proxy;

import com.whoiszxl.rpc.core.client.RpcReferenceWrapper;

/**
 * 代理工厂
 */
public interface ProxyFactory {

    /**
     * 传入类获取代理
     * @param clazz
     * @param <T>
     * @return
     * @throws Throwable
     */
    <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;
}