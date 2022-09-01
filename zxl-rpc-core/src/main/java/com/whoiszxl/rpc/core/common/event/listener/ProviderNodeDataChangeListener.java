package com.whoiszxl.rpc.core.common.event.listener;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.event.RpcListener;
import com.whoiszxl.rpc.core.common.event.RpcNodeChangeEvent;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.zk.ProviderNodeInfo;

import java.util.List;

public class ProviderNodeDataChangeListener implements RpcListener<RpcNodeChangeEvent> {

    @Override
    public void callback(Object t) {
        ProviderNodeInfo providerNodeInfo = (ProviderNodeInfo) t;
        List<ChannelFutureWrapper> channelFutureWrapperList = RpcClientCache.CONNECT_MAP.get(providerNodeInfo.getServiceName());

        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrapperList) {
            String address = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())) {
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                RegURL regURL = new RegURL();
                regURL.setServiceName(providerNodeInfo.getServiceName());

                RpcClientCache.IROUTER.updateWeight(regURL);
                break;
            }
        }
    }
}
