package com.whoiszxl.rpc.core.filter;

import com.whoiszxl.rpc.core.common.pack.RpcInvocation;

public interface IServerFilter extends IFilter {

    void doFilter(RpcInvocation rpcInvocation);
}
