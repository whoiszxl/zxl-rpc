package com.whoiszxl.rpc.core.filter.client;

import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

public class ClientFilterChain {

    private static List<IClientFilter> iClientFilters = new ArrayList<>();

    public void addClientFilter(IClientFilter iClientFilter) {
        iClientFilters.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (IClientFilter iClientFilter : iClientFilters) {
            iClientFilter.doFilter(src, rpcInvocation);
        }
    }
}
