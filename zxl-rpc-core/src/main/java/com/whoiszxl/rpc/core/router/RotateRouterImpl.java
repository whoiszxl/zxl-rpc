package com.whoiszxl.rpc.core.router;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.registy.RegURL;

import java.util.List;

public class RotateRouterImpl implements IRouter{

    @Override
    public void refreshRouterArr(Selector selector) {
        //将对应服务的正在连接中的转数组存入路由map中
        List<ChannelFutureWrapper> channelFutureWrapperList = RpcClientCache.CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrapperList.size()];

        for (int i = 0; i < channelFutureWrapperList.size(); i++) {
            arr[i] = channelFutureWrapperList.get(i);
        }

        RpcClientCache.SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
    }

    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return RpcClientCache.CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    @Override
    public void updateWeight(RegURL regURL) {

    }
}
