package com.whoiszxl.rpc.core.client;

import com.whoiszxl.rpc.core.proxy.ProxyFactory;

public class RpcReference {

    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public <T> T get(RpcReferenceWrapper<T> referenceWrapper) throws Throwable {
        return proxyFactory.getProxy(referenceWrapper);
    }
}
