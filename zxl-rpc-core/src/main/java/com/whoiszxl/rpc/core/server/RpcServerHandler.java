package com.whoiszxl.rpc.core.server;

import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.common.pack.RpcProtocol;
import com.whoiszxl.rpc.core.dispatcher.ServerChannelDispatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * rpc server处理器
 */
public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收到客户端发送过来的消息后，将消息与连接处理器进行包装，放入分发器队列中
        System.out.println("获取到请求" + msg);
        ServerChannelReadData data = new ServerChannelReadData();
        data.setRpcProtocol((RpcProtocol) msg);
        data.setChannelHandlerContext(ctx);

        RpcServerCache.SERVER_CHANNEL_DISPATCHER.add(data);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if(channel.isActive()) {
            ctx.close();
        }
    }
}
