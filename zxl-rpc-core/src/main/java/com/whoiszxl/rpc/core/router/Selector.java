package com.whoiszxl.rpc.core.router;

import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;

/**
 * 选择器
 */
public class Selector {

    /**
     * 提供者的服务名称
     */
    private String providerServiceName;


    private ChannelFutureWrapper[] channelFutureWrappers;

    public ChannelFutureWrapper[] getChannelFutureWrappers() {
        return channelFutureWrappers;
    }

    public void setChannelFutureWrappers(ChannelFutureWrapper[] channelFutureWrappers) {
        this.channelFutureWrappers = channelFutureWrappers;
    }

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }
}
