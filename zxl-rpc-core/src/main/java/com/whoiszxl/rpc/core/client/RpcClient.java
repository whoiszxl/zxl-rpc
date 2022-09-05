package com.whoiszxl.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.config.PropertiesBootstrap;
import com.whoiszxl.rpc.core.common.config.RpcClientConfig;
import com.whoiszxl.rpc.core.common.event.RpcListenerLoader;
import com.whoiszxl.rpc.core.common.pack.RpcDecoder;
import com.whoiszxl.rpc.core.common.pack.RpcEncoder;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.common.pack.RpcProtocol;
import com.whoiszxl.rpc.core.common.utils.IpUtils;
import com.whoiszxl.rpc.core.filter.IClientFilter;
import com.whoiszxl.rpc.core.filter.client.ClientFilterChain;
import com.whoiszxl.rpc.core.proxy.ProxyFactory;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.RegistryService;
import com.whoiszxl.rpc.core.registy.zk.AbstractRegister;
import com.whoiszxl.rpc.core.router.IRouter;
import com.whoiszxl.rpc.core.serialize.SerializeFactory;
import com.whoiszxl.rpc.core.spi.ExtensionLoader;
import com.whoiszxl.rpc.service.LoginService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RpcClient {

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private RpcClientConfig rpcClientConfig;

    private RpcListenerLoader rpcListenerLoader;

    private Bootstrap bootstrap = new Bootstrap();


    /**
     * 启动rpc客户端
     * @return
     */
    public RpcReference initClient() throws InterruptedException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new RpcClientHandler());
            }
        });

        rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();
        this.rpcClientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        RpcClientCache.CLIENT_CONFIG = this.rpcClientConfig;

        //通过自定义spi的方式加载代理方式
        RpcReference rpcReference;
        ExtensionLoader.LOADER_INSTANCE.loadExtension(ProxyFactory.class);
        LinkedHashMap<String, Class> proxyMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(ProxyFactory.class.getName());
        Class proxyClass = proxyMap.get(rpcClientConfig.getProxyType());
        rpcReference = new RpcReference((ProxyFactory) proxyClass.newInstance());
        return rpcReference;
    }

    public void doSubscribeService(Class<?> serviceBean) {
        if (RpcClientCache.ABSTRACT_REGISTER == null) {
            try{
                ExtensionLoader.LOADER_INSTANCE.loadExtension(RegistryService.class);
                LinkedHashMap<String, Class> registerMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registerClass = registerMap.get(rpcClientConfig.getRegisterType());
                RpcClientCache.ABSTRACT_REGISTER = (AbstractRegister) registerClass.newInstance();
            }catch (Exception e) {
                throw new RuntimeException("注册服务未知异常", e);
            }
        }
        RegURL url = new RegURL();
        url.setApplicationName(rpcClientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", IpUtils.getIpAddress());

        Map<String, String> result = RpcClientCache.ABSTRACT_REGISTER.getServiceWeightMap(serviceBean.getName());
        RpcClientCache.URL_MAP.put(serviceBean.getName(), result);

        RpcClientCache.ABSTRACT_REGISTER.subscribe(url);
    }


    public void doConnectServer() {
        //遍历所有的service服务
        for (RegURL providerUrl : RpcClientCache.SUBSCRIBE_SERVICE_LIST) {
            //从zk中拿到所有的服务提供者的ip:port
            List<String> providerIps = RpcClientCache.ABSTRACT_REGISTER.getProviderIps(providerUrl.getServiceName());
            //遍历所有的ip:port，创建连接，并保存到CONNECT_MAP连接缓存中去
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerUrl.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }
            //订阅变动
            RegURL url = new RegURL();
            url.addParameter("servicePath", providerUrl.getServiceName() + "/provider");
            url.addParameter("providerIps", JSON.toJSONString(providerIps));
            RpcClientCache.ABSTRACT_REGISTER.doAfterSubscribe(url);
        }
    }


    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }


    static class AsyncSendJob implements Runnable {
        @Override
        public void run() {
            while(true) {
                try{
                    //死循环获取队列中的发送请求，并通过自定义序列化方式序列化参数，包装到自定义协议中
                    RpcInvocation rpcInvocation = RpcClientCache.SEND_QUEUE.take();
                    RpcProtocol rpcProtocol = new RpcProtocol(RpcClientCache.CLIENT_SERIALIZE_FACTORY.serialize(rpcInvocation));

                    //获取到目标机器的netty连接，然后进行发送
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(rpcInvocation);
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static void main(String[] args) throws Throwable {
        RpcClient rpcClient = new RpcClient();
        RpcReference rpcReference = rpcClient.initClient();

        //初始化客户端配置
        rpcClient.initClientConfig();

        RpcReferenceWrapper<LoginService> rpcReferenceWrapper = new RpcReferenceWrapper<>();
        rpcReferenceWrapper.setAimClass(LoginService.class);
        rpcReferenceWrapper.setGroup("dev");
        rpcReferenceWrapper.setServiceToken("token-zxl");


        //通过代理的方式获取到服务,在代理中将请求封装到队列里，然后将结果重新返回到队列中，通过超时的判断将结果返回
        LoginService loginService = rpcReference.get(rpcReferenceWrapper);
        rpcClient.doSubscribeService(LoginService.class);

        ConnectionHandler.setBootstrap(rpcClient.getBootstrap());
        rpcClient.doConnectServer();
        rpcClient.startClient();

        for (int i = 0; i < 100; i++) {
            String token = loginService.login("zxl" + (i + 1), "zxl_password" + (i + 1));
            System.out.println(token);
        }


    }

    private void initClientConfig() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        //从自定义SPI加载路由实现类
        ExtensionLoader.LOADER_INSTANCE.loadExtension(IRouter.class);

        //取出对应配置的类并实例化为对象
        String routerStrategy = rpcClientConfig.getRouterStrategy();
        LinkedHashMap<String, Class> routerMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(IRouter.class.getName());
        Class routerClass = routerMap.get(routerStrategy);
        if(routerClass == null) {
            throw new RuntimeException("路由配置不合法");
        }
        RpcClientCache.IROUTER = (IRouter) routerClass.newInstance();

        //从自定义SPI加载序列化实现
        ExtensionLoader.LOADER_INSTANCE.loadExtension(SerializeFactory.class);
        String clientSerialize = rpcClientConfig.getClientSerialize();
        LinkedHashMap<String, Class> serializeMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeClass = serializeMap.get(clientSerialize);
        if(serializeClass == null) {
            throw new RuntimeException("序列化配置不合法");
        }
        RpcClientCache.CLIENT_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();

        //从自定义SPI加载过滤链
        ExtensionLoader.LOADER_INSTANCE.loadExtension(IClientFilter.class);
        LinkedHashMap<String, Class> filterChainMap = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(IClientFilter.class.getName());

        ClientFilterChain clientFilterChain = new ClientFilterChain();
        for (String key : filterChainMap.keySet()) {
            Class aClass = filterChainMap.get(key);
            if(aClass == null) {
                throw new RuntimeException("过滤调用链配置不合法");
            }
            clientFilterChain.addClientFilter((IClientFilter) aClass.newInstance());
        }

        RpcClientCache.CLIENT_FILTER_CHAIN = clientFilterChain;
    }


    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public RpcClientConfig getRpcClientConfig() {
        return rpcClientConfig;
    }

    public void setRpcClientConfig(RpcClientConfig rpcClientConfig) {
        this.rpcClientConfig = rpcClientConfig;
    }
}
