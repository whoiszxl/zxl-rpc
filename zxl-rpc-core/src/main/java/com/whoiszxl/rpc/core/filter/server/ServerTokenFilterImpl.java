package com.whoiszxl.rpc.core.filter.server;

import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IServerFilter;
import com.whoiszxl.rpc.core.server.ServiceWrapper;

public class ServerTokenFilterImpl implements IServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        ServiceWrapper serviceWrapper = RpcServerCache.PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());

        //如果matchToken不存在，说明不需要鉴权
        String matchToken = String.valueOf(serviceWrapper.getToken());
        if(matchToken == null || matchToken.equals("")) {
            return;
        }

        //如果传入的token和匹配token一致，则鉴权通过
        if(matchToken.equals(token)) {
            return;
        }

        throw new RuntimeException("token:[" + token + "]鉴权失败");
    }
}
