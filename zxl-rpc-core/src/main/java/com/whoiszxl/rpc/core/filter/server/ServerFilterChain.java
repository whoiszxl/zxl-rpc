package com.whoiszxl.rpc.core.filter.server;

import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IServerFilter;

import java.util.ArrayList;
import java.util.List;

public class ServerFilterChain {

    /**
     * 调用链
     */
    private static List<IServerFilter> iServerFilters = new ArrayList<>();

    /**
     * 新增过滤器到调用链里
     * @param iServerFilter
     */
    public void addServerFilter(IServerFilter iServerFilter) {
        iServerFilters.add(iServerFilter);
    }

    /**
     * 执行调用链，for循环依次调用
     * @param rpcInvocation
     */
    public void doFilter(RpcInvocation rpcInvocation) {
        for (IServerFilter iServerFilter : iServerFilters) {
            iServerFilter.doFilter(rpcInvocation);
        }
    }

}
