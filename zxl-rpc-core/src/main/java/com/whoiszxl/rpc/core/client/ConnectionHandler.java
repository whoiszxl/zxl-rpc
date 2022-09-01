package com.whoiszxl.rpc.core.client;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.router.Selector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

import java.util.*;

public class ConnectionHandler {

    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if(bootstrap == null) {
            throw new RuntimeException("发起连接时bootstrap不能为空");
        }

        if(!providerIp.contains(":")) {
            return;
        }

        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        int port = Integer.parseInt(providerAddress[1]);

        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerURLInfo = RpcClientCache.URL_MAP.get(providerServiceName).get(providerIp);

        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setWeight(Integer.valueOf(providerURLInfo.substring(providerURLInfo.lastIndexOf(";") + 1)));
        RpcClientCache.SERVER_ADDRESS.add(providerIp);

        List<ChannelFutureWrapper> channelFutureWrapperList = RpcClientCache.CONNECT_MAP.get(providerServiceName);
        if(channelFutureWrapperList == null || channelFutureWrapperList.isEmpty()) {
            channelFutureWrapperList = new ArrayList<>();
        }

        channelFutureWrapperList.add(channelFutureWrapper);
        RpcClientCache.CONNECT_MAP.put(providerServiceName, channelFutureWrapperList);

        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        RpcClientCache.IROUTER.refreshRouterArr(selector);
    }

    public static ChannelFuture createChannelFuture(String ip, Integer port) throws InterruptedException {
        return bootstrap.connect(ip, port).sync();
    }

    public static void disConnect(String providerServiceName, String providerIp) {
        RpcClientCache.SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = RpcClientCache.CONNECT_MAP.get(providerServiceName);
        if (channelFutureWrappers != null && !channelFutureWrappers.isEmpty()) {
            channelFutureWrappers.removeIf(channelFutureWrapper -> providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()));
        }
    }

    /**
     * 获取netty的channel连接
     * @param providerServiceName
     * @return
     */
    public static ChannelFuture getChannelFuture(String providerServiceName) {
        List<ChannelFutureWrapper> channelFutureWrappers = RpcClientCache.CONNECT_MAP.get(providerServiceName);
        if (channelFutureWrappers == null || channelFutureWrappers.isEmpty()) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }

        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        return RpcClientCache.IROUTER.select(selector).getChannelFuture();
    }


}
