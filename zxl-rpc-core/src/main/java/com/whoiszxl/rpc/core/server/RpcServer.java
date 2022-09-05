package com.whoiszxl.rpc.core.server;

import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.config.PropertiesBootstrap;
import com.whoiszxl.rpc.core.common.config.RpcServerConfig;
import com.whoiszxl.rpc.core.common.constants.RpcConstants;
import com.whoiszxl.rpc.core.common.event.RpcListenerLoader;
import com.whoiszxl.rpc.core.common.pack.RpcDecoder;
import com.whoiszxl.rpc.core.common.pack.RpcEncoder;
import com.whoiszxl.rpc.core.common.utils.IpUtils;
import com.whoiszxl.rpc.core.filter.IServerFilter;
import com.whoiszxl.rpc.core.filter.server.ServerFilterChain;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.RegistryService;
import com.whoiszxl.rpc.core.serialize.SerializeFactory;
import com.whoiszxl.rpc.core.service.impl.LoginServiceImpl;
import com.whoiszxl.rpc.core.spi.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * rpc netty server
 */
public class RpcServer {

    private static EventLoopGroup bossGroup = null;

    private static EventLoopGroup workerGroup = null;

    private static RpcListenerLoader rpcListenerLoader;



    /**
     * start netty server app.
     */
    public void startApp() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(3);

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
                ByteBuf delimiter = Unpooled.copiedBuffer(RpcConstants.DEFAULT_DECODE_CHAR.getBytes());
                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(4096, delimiter));
                socketChannel.pipeline().addLast(new RpcEncoder());
                socketChannel.pipeline().addLast(new RpcDecoder());
                socketChannel.pipeline().addLast(new RpcServerHandler());
            }
        });

        this.batchExportUrl();

        RpcServerCache.SERVER_CHANNEL_DISPATCHER.startDataConsume();

        serverBootstrap.bind(RpcServerCache.SERVER_CONFIG.getServerPort()).sync();

        System.out.println("服务启动成功: " + IpUtils.getIpAddress() + ":" + RpcServerCache.SERVER_CONFIG.getServerPort());
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
    public void registerService(ServiceWrapper serviceWrapper) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Object serviceBean = serviceWrapper.getServiceObj();
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();

        if(interfaces.length == 0) {
            throw new RuntimeException("注册的服务必须要实现接口");
        }

        if(interfaces.length > 1) {
            throw new RuntimeException("注册的服务只能实现一个接口");
        }

        //通过SPI创建注册服务
        if(RpcServerCache.REGISTRY_SERVICE == null) {
            ExtensionLoader.LOADER_INSTANCE.loadExtension(RegistryService.class);
            LinkedHashMap<String, Class> registryMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
            Class registryClass = registryMap.get(RpcServerCache.SERVER_CONFIG.getRegisterType());
            if(registryClass == null) {
                throw new RuntimeException("注册中心配置不合法");
            }
            RpcServerCache.REGISTRY_SERVICE = (RegistryService) registryClass.newInstance();
        }

        Class<?> myInterface = interfaces[0];

        RpcServerCache.PROVIDER_CLASS_MAP.put(myInterface.getName(), serviceBean);
        RegURL regURL = new RegURL();
        regURL.setServiceName(myInterface.getName());
        regURL.setApplicationName(RpcServerCache.SERVER_CONFIG.getApplicationName());
        regURL.addParameter("host", IpUtils.getIpAddress());
        regURL.addParameter("port", String.valueOf(RpcServerCache.SERVER_CONFIG.getServerPort()));
        regURL.addParameter("group", String.valueOf(serviceWrapper.getGroup()));
        regURL.addParameter("limit", String.valueOf(serviceWrapper.getLimit()));
        RpcServerCache.PROVIDER_URL_SET.add(regURL);

        if(!"".equals(serviceWrapper.getToken())) {
            RpcServerCache.PROVIDER_SERVICE_WRAPPER_MAP.put(myInterface.getName(), serviceWrapper);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
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

    private void initServerConfig() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        //加载properties配置
        RpcServerConfig rpcServerConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        RpcServerCache.SERVER_CONFIG = rpcServerConfig;

        RpcServerCache.SERVER_CHANNEL_DISPATCHER.init(rpcServerConfig.getServerQueueSize(), rpcServerConfig.getServerBizThreadNums());

        //加载服务端序列化方式
        String serverSerialize = rpcServerConfig.getServerSerialize();

        ExtensionLoader.LOADER_INSTANCE.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class> serializeMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeClass = serializeMap.get(serverSerialize);
        RpcServerCache.SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();

        ServerFilterChain serverFilterChain = new ServerFilterChain();
        ExtensionLoader.LOADER_INSTANCE.loadExtension(IServerFilter.class);
        LinkedHashMap<String, Class> filterChainMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(IServerFilter.class.getName());
        for (Class aClass : filterChainMap.values()) {
            if(aClass == null) {
                throw new RuntimeException("过滤调用链不合法");
            }
            serverFilterChain.addServerFilter((IServerFilter) aClass.newInstance());
        }
        RpcServerCache.SERVER_FILTER_CHAIN = serverFilterChain;
    }
}
