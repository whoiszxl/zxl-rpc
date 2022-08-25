package com.whoiszxl.rpc.core.proxy;

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
    <T> T getProxy(final Class<T> clazz) throws Throwable;
}