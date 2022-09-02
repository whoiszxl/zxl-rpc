package com.whoiszxl.rpc.core.server;

import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.config.PropertiesBootstrap;
import com.whoiszxl.rpc.core.common.config.RpcServerConfig;
import com.whoiszxl.rpc.core.common.event.RpcListenerLoader;
import com.whoiszxl.rpc.core.common.pack.RpcDecoder;
import com.whoiszxl.rpc.core.common.pack.RpcEncoder;
import com.whoiszxl.rpc.core.common.utils.IpUtils;
import com.whoiszxl.rpc.core.filter.server.ServerFilterChain;
import com.whoiszxl.rpc.core.filter.server.ServerLogFilterImpl;
import com.whoiszxl.rpc.core.filter.server.ServerTokenFilterImpl;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.zk.ZookeeperRegister;
import com.whoiszxl.rpc.core.serialize.jdk.JdkSerializeFactory;
import com.whoiszxl.rpc.core.serialize.kryo.KryoSerializeFactory;
import com.whoiszxl.rpc.core.service.impl.LoginServiceImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * rpc netty server
 */
public class RpcServer {

    private static EventLoopGroup bossGroup = null;

    private static EventLoopGroup workerGroup = null;

    private RpcServerConfig rpcServerConfig;

    private static RpcListenerLoader rpcListenerLoader;



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

        this.batchExportUrl();

        serverBootstrap.bind(rpcServerConfig.getServerPort()).sync();

        System.out.println("服务启动成功: " + IpUtils.getIpAddress() + ":" + rpcServerConfig.getServerPort());
    }

    public void batchExportUrl() {
        Thread task = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (RegURL regURL : RpcServerCache.PROVIDER_URL_SET) {
                RpcServerCache.REGISTRY_SERVICE.register(regURL);
            }
        });

        task.start();
    }


    /**
     * 将我们自定义的服务注册到服务端里
     * 自定义服务必须实现接口，并且只能实现一个
     *
     */
    public void registerService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceObj();
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();

        if(interfaces.length == 0) {
            throw new RuntimeException("注册的服务必须要实现接口");
        }

        if(interfaces.length > 1) {
            throw new RuntimeException("注册的服务只能实现一个接口");
        }

        //创建注册服务
        if(RpcServerCache.REGISTRY_SERVICE == null) {
            RpcServerCache.REGISTRY_SERVICE = new ZookeeperRegister(rpcServerConfig.getRegisterAddr());
        }

        Class<?> myInterface = interfaces[0];

        RpcServerCache.PROVIDER_CLASS_MAP.put(myInterface.getName(), serviceBean);
        RegURL regURL = new RegURL();
        regURL.setServiceName(myInterface.getName());
        regURL.setApplicationName(rpcServerConfig.getApplicationName());
        regURL.addParameter("host", IpUtils.getIpAddress());
        regURL.addParameter("port", String.valueOf(rpcServerConfig.getServerPort()));
        regURL.addParameter("group", String.valueOf(serviceWrapper.getGroup()));
        regURL.addParameter("limit", String.valueOf(serviceWrapper.getLimit()));
        RpcServerCache.PROVIDER_URL_SET.add(regURL);

        if(!"".equals(serviceWrapper.getToken())) {
            RpcServerCache.PROVIDER_SERVICE_WRAPPER_MAP.put(myInterface.getName(), serviceWrapper);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RpcServer rpcServer = new RpcServer();
        rpcServer.initServerConfig();

        rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();

        ServiceWrapper serviceWrapper = new ServiceWrapper(new LoginServiceImpl(), "dev");
        serviceWrapper.setToken("token-zxl");
        serviceWrapper.setLimit(2);

        rpcServer.registerService(serviceWrapper);
        rpcServer.startApp();
    }

    private void initServerConfig() {
        //加载properties配置
        RpcServerConfig rpcServerConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setRpcServerConfig(rpcServerConfig);

        //加载服务端序列化方式
        String serverSerialize = rpcServerConfig.getServerSerialize();

        switch (serverSerialize) {
            case "jdk":
                RpcServerCache.SERVER_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            default:
                RpcServerCache.SERVER_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
        }

        ServerFilterChain serverFilterChain = new ServerFilterChain();
        serverFilterChain.addServerFilter(new ServerLogFilterImpl());
        serverFilterChain.addServerFilter(new ServerTokenFilterImpl());
        RpcServerCache.SERVER_FILTER_CHAIN = serverFilterChain;
    }


    public RpcServerConfig getRpcServerConfig() {
        return rpcServerConfig;
    }

    public void setRpcServerConfig(RpcServerConfig rpcServerConfig) {
        this.rpcServerConfig = rpcServerConfig;
    }
}
