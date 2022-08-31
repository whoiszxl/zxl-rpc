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
    public RpcReference startClientApp() throws InterruptedException {
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
        abstractRegister.subscribe(url);
    }


    public void doConnectServer() {
        for (String providerServiceName : RpcClientCache.SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerServiceName);
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerServiceName, providerIp);
                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }
            RegURL url = new RegURL();
            url.setServiceName(providerServiceName);
            abstractRegister.doAfterSubscribe(url);
        }
    }


    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }


    class AsyncSendJob implements Runnable {

        @Override
        public void run() {
            while(true) {
                try{

                    RpcInvocation data = RpcClientCache.SEND_QUEUE.take();
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());

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
        RpcReference rpcReference = rpcClient.startClientApp();


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
