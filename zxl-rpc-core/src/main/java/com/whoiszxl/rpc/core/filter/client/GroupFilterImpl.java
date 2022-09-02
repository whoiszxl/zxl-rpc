package com.whoiszxl.rpc.core.filter.client;

import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IClientFilter;

import java.util.List;

public class GroupFilterImpl implements IClientFilter {

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        for (ChannelFutureWrapper channelFutureWrapper : src) {
            if(!group.equals(channelFutureWrapper.getGroup())) {
                src.remove(channelFutureWrapper);
            }
        }

        if(src.isEmpty()) {
            throw new RuntimeException("没有匹配到分组：" + group);
        }
    }
}
