package com.whoiszxl.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.config.RpcClientConfig;
import com.whoiszxl.rpc.core.common.pack.RpcDecoder;
import com.whoiszxl.rpc.core.common.pack.RpcEncoder;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.common.pack.RpcProtocol;
import com.whoiszxl.rpc.core.proxy.jdk.JDKProxyFactory;
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

public class RpcClient {

    public static EventLoopGroup clientGroup;

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);


    private RpcClientConfig rpcClientConfig;

    /**
     * 启动rpc客户端
     * @return
     */
    public RpcReference startClientApp() throws InterruptedException {
        clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
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

        //连接到服务提供者的ip+port
        ChannelFuture channelFuture = bootstrap.connect(rpcClientConfig.getServerHost(), rpcClientConfig.getPort()).sync();

        //另起线程，死循环从请求队列中获取数据包进行发送
        this.startClient(channelFuture);

        //返回rpc的引用
        return new RpcReference(new JDKProxyFactory());
    }



    private void startClient(ChannelFuture channelFuture) {
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }


    class AsyncSendJob implements Runnable {

        private final ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        @Override
        public void run() {
            while(true) {
                try{

                    RpcInvocation data = RpcClientCache.SEND_QUEUE.take();
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());

                    channelFuture.channel().writeAndFlush(rpcProtocol);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) throws Throwable {
        RpcClient rpcClient = new RpcClient();
        RpcClientConfig rpcClientConfig = new RpcClientConfig();
        rpcClientConfig.setPort(10000);
        rpcClientConfig.setServerHost("127.0.0.1");
        rpcClient.setRpcClientConfig(rpcClientConfig);

        RpcReference rpcReference = rpcClient.startClientApp();
        //通过代理的方式获取到服务,在代理中将请求封装到队列里，然后将结果重新返回到队列中，通过超时的判断将结果返回
        LoginService loginService = rpcReference.get(LoginService.class);

        for (int i = 0; i < 100; i++) {
            String token = loginService.login("zxl" + (i + 1), "zxl_password" + (i + 1));
            System.out.println(token);
        }


    }



    public void setRpcClientConfig(RpcClientConfig rpcClientConfig) {
        this.rpcClientConfig = rpcClientConfig;
    }
}
