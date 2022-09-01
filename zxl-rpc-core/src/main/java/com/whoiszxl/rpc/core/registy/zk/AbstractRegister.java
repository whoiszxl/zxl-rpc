package com.whoiszxl.rpc.core.registy.zk;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.RegistryService;

import java.util.List;
import java.util.Map;

public abstract class AbstractRegister implements RegistryService {

    @Override
    public void register(RegURL url) {
        RpcServerCache.PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(RegURL url) {
        RpcServerCache.PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(RegURL url) {
        RpcClientCache.SUBSCRIBE_SERVICE_LIST.add(url);
    }

    @Override
    public void doUnSubscribe(RegURL url) {
        RpcClientCache.SUBSCRIBE_SERVICE_LIST.remove(url);
    }


    public abstract void doAfterSubscribe(RegURL url);

    public abstract void doBeforeSubscribe(RegURL url);

    public abstract List<String> getProviderIps(String serviceName);

    public abstract Map<String, String> getServiceWeightMap(String name);
}
