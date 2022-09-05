package com.whoiszxl.rpc.core.common.event.listener;

import com.whoiszxl.rpc.core.client.ConnectionHandler;
import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.event.RpcListener;
import com.whoiszxl.rpc.core.common.event.RpcUpdateEvent;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.event.data.URLChangeWrapper;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceUpdateListener implements RpcListener<RpcUpdateEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUpdateListener.class);


    @Override
    public void callback(Object t) {
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;

        //从当前连接map中获取对应服务的netty连接
        List<ChannelFutureWrapper> channelFutureWrappers = RpcClientCache.CONNECT_MAP.get(urlChangeWrapper.getServiceName());

        if(channelFutureWrappers == null || channelFutureWrappers.size() < 1) {
            logger.error("channelFutureWrappers为空");
            return;
        }

        List<String> matchProviderUrls = urlChangeWrapper.getProviderUrl();

        Set<String> finalUrlSet = new HashSet<>();
        List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();

        //遍历当前服务中netty连接
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();

            //如果传入的提供者地址列表中包含了老的地址，就将其添加到final中
            if(matchProviderUrls.contains(oldServerAddress)) {
                finalChannelFutureWrappers.add(channelFutureWrapper);
                finalUrlSet.add(oldServerAddress);
            }
        }


        //此时final中已经移除旧的连接
        List<ChannelFutureWrapper> newChannelFutureWrappers = new ArrayList<>();

        //遍历传入的新的提供者地址
        for (String newProviderUrl : matchProviderUrls) {

            //如果传入的不在当前系统维护的连接内，就创建新的netty连接，封装到channelFutureWrapper中去
            if(!finalUrlSet.contains(newProviderUrl)) {
                ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                String host = newProviderUrl.split(":")[0];
                Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);

                channelFutureWrapper.setHost(host);
                channelFutureWrapper.setPort(port);

                ChannelFuture channelFuture = null;

                try {
                    channelFuture = ConnectionHandler.createChannelFuture(host, port);
                    channelFutureWrapper.setChannelFuture(channelFuture);
                    newChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrlSet.add(newProviderUrl);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        finalChannelFutureWrappers.addAll(newChannelFutureWrappers);
        RpcClientCache.CONNECT_MAP.put(urlChangeWrapper.getServiceName(), finalChannelFutureWrappers);
    }
}
