package com.whoiszxl.rpc.core.filter.client;

import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.filter.IClientFilter;

import java.util.Iterator;
import java.util.List;

public class DirectInvokeFilterImpl implements IClientFilter {


    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String url = (String) rpcInvocation.getAttachments().get("url");
        if(url == null || url.isEmpty()) {
            return;
        }

        Iterator<ChannelFutureWrapper> iterator = src.iterator();
        while(iterator.hasNext()) {
            ChannelFutureWrapper channelFutureWrapper = iterator.next();
            if(!url.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
                iterator.remove();
            }
        }

        if(src.isEmpty()) {
            throw new RuntimeException("没有匹配到URL：" + url);
        }

    }
}
