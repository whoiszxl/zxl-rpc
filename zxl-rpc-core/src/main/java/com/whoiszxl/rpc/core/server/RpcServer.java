package com.whoiszxl.rpc.core.server;

import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.config.RpcServerConfig;
import com.whoiszxl.rpc.core.common.pack.RpcDecoder;
import com.whoiszxl.rpc.core.common.pack.RpcEncoder;
import com.whoiszxl.rpc.core.service.impl.LoginServiceImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * rpc netty server
 */
public class RpcServer {

    private static EventLoopGroup bossGroup = null;

    private static EventLoopGroup workerGroup = null;

    private RpcServerConfig rpcServerConfig;


    /**
     * start netty server app.
     */
    public void startApp() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024);
        serverBootstrap.option(ChannelOption.SO_RCVBUF, 16 * 1024);
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                System.out.println("init rpc server.");
                socketChannel.pipeline().addLast(new RpcEncoder());
                socketChannel.pipeline().addLast(new RpcDecoder());
                socketChannel.pipeline().addLast(new RpcServerHandler());
            }
        });

        serverBootstrap.bind(rpcServerConfig.getPort()).sync();
    }


    /**
     * 将我们自定义的服务注册到服务端里
     * 自定义服务必须实现接口，并且只能实现一个
     *
     * @param serviceBean service bean.
     */
    public void registerService(Object serviceBean) {
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();

        if(interfaces.length == 0) {
            throw new RuntimeException("注册的服务必须要实现接口");
        }

        if(interfaces.length > 1) {
            throw new RuntimeException("注册的服务只能实现一个接口");
        }

        Class<?> myInterface = interfaces[0];

        RpcServerCache.classCache.put(myInterface.getName(), serviceBean);

    }

    public static void main(String[] args) throws InterruptedException {
        RpcServer rpcServer = new RpcServer();
        RpcServerConfig rpcServerConfig = new RpcServerConfig();
        rpcServerConfig.setPort(10000);
        rpcServer.setRpcServerConfig(rpcServerConfig);
        rpcServer.registerService(new LoginServiceImpl());
        rpcServer.startApp();
    }



    public RpcServerConfig getRpcServerConfig() {
        return rpcServerConfig;
    }

    public void setRpcServerConfig(RpcServerConfig rpcServerConfig) {
        this.rpcServerConfig = rpcServerConfig;
    }
}
