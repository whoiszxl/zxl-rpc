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
import com.whoiszxl.rpc.core.proxy.jdk.JDKProxyFactory;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.zk.AbstractRegister;
import com.whoiszxl.rpc.core.registy.zk.ZookeeperRegister;
import com.whoiszxl.rpc.core.router.RandomRouterImpl;
import com.whoiszxl.rpc.core.router.RotateRouterImpl;
import com.whoiszxl.rpc.core.serialize.jdk.JdkSerializeFactory;
import com.whoiszxl.rpc.core.serialize.kryo.KryoSerializeFactory;
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

import java.util.List;
import java.util.Map;

public class RpcClient {

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private RpcClientConfig rpcClientConfig;

    private AbstractRegister abstractRegister;

    private RpcListenerLoader rpcListenerLoader;

    private Bootstrap bootstrap = new Bootstrap();


    /**
     * 启动rpc客户端
     * @return
     */
    public RpcReference initClient() throws InterruptedException {
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

        RpcReference rpcReference;

        if("javassist".equals(rpcClientConfig.getProxyType())) {
            //todo
            rpcReference = new RpcReference(new JDKProxyFactory());
        }else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }

        return rpcReference;
    }

    public void doSubscribeService(Class<?> serviceBean) {
        if (abstractRegister == null) {
            abstractRegister = new ZookeeperRegister(rpcClientConfig.getRegisterAddr());
        }
        RegURL url = new RegURL();
        url.setApplicationName(rpcClientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", IpUtils.getIpAddress());

        Map<String, String> result = abstractRegister.getServiceWeightMap(serviceBean.getName());
        RpcClientCache.URL_MAP.put(serviceBean.getName(), result);

        abstractRegister.subscribe(url);
    }


    public void doConnectServer() {
        //遍历所有的service服务
        for (RegURL providerUrl : RpcClientCache.SUBSCRIBE_SERVICE_LIST) {
            //从zk中拿到所有的服务提供者的ip:port
            List<String> providerIps = abstractRegister.getProviderIps(providerUrl.getServiceName());
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
            abstractRegister.doAfterSubscribe(url);
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
                    RpcInvocation data = RpcClientCache.SEND_QUEUE.take();
                    RpcProtocol rpcProtocol = new RpcProtocol(RpcClientCache.CLIENT_SERIALIZE_FACTORY.serialize(data));

                    //获取到目标机器的netty连接，然后进行发送
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
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

        //通过代理的方式获取到服务,在代理中将请求封装到队列里，然后将结果重新返回到队列中，通过超时的判断将结果返回
        LoginService loginService = rpcReference.get(LoginService.class);
        rpcClient.doSubscribeService(LoginService.class);

        ConnectionHandler.setBootstrap(rpcClient.getBootstrap());
        rpcClient.doConnectServer();
        rpcClient.startClient();

        for (int i = 0; i < 100; i++) {
            String token = loginService.login("zxl" + (i + 1), "zxl_password" + (i + 1));
            System.out.println(token);
        }


    }

    private void initClientConfig() {
        String routerStrategy = rpcClientConfig.getRouterStrategy();
        if("rotate".equals(routerStrategy)) {
            RpcClientCache.IROUTER = new RotateRouterImpl();
        }else if("random".equals(routerStrategy)) {
            RpcClientCache.IROUTER = new RandomRouterImpl();
        }

        String clientSerialize = rpcClientConfig.getClientSerialize();
        switch (clientSerialize) {
            case "jdk":
                RpcClientCache.CLIENT_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            default:
                RpcClientCache.CLIENT_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
        }
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
