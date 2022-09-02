package com.whoiszxl.rpc.core.filter;

import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;

import java.util.List;

public interface IClientFilter extends IFilter {

    /**
     * 执行责任链
     * @param src
     * @param rpcInvocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);

}
