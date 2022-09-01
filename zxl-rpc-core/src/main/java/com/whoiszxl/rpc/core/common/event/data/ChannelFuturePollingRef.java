package com.whoiszxl.rpc.core.common.event.data;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;

import java.util.concurrent.atomic.AtomicLong;

public class ChannelFuturePollingRef {


    private AtomicLong referenceTimes = new AtomicLong(0);

    /**
     * 通过自增取余的方式轮询获取数组中的通道连接
     * @param serviceName
     * @return
     */
    public ChannelFutureWrapper getChannelFutureWrapper(String serviceName) {
        ChannelFutureWrapper[] arr = RpcClientCache.SERVICE_ROUTER_MAP.get(serviceName);
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }
}
