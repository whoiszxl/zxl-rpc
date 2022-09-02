package com.whoiszxl.rpc.core.filter.client;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientLogFilterImpl implements IClientFilter {

    private static final Logger logger = LoggerFactory.getLogger(ClientLogFilterImpl.class);

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        rpcInvocation.getAttachments().put("c_app_name", RpcClientCache.CLIENT_CONFIG.getApplicationName());
        logger.info("调用日志：{} ----> {}.{}", rpcInvocation.getAttachments().get("c_app_name"), rpcInvocation.getTargetServiceName(), rpcInvocation.getTargetMethod());
    }
}
