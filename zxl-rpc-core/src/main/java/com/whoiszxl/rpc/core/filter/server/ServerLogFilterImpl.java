package com.whoiszxl.rpc.core.filter.server;

import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLogFilterImpl implements IServerFilter {

    private static final Logger logger = LoggerFactory.getLogger(ServerLogFilterImpl.class);

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        logger.info("服务端调用：{} ---> {}.{}",
                rpcInvocation.getAttachments().get("c_app_name"),
                rpcInvocation.getTargetServiceName(),
                rpcInvocation.getTargetMethod());
    }
}
