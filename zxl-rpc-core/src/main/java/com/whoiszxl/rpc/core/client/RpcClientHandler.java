package com.whoiszxl.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.common.pack.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc 客户端处理器
 */
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取到rpc调用类
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        byte[] content = rpcProtocol.getContent();
        String json = new String(content, 0, content.length);
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);

        //如果uuid与发送时的不一致，表明response是不合法的
        if(!RpcClientCache.RESPONSE_CACHES.containsKey(rpcInvocation.getUuid())) {
            throw new IllegalArgumentException("返回参数不合法");
        }

        //将调用结果存回，释放msg
        RpcClientCache.RESPONSE_CACHES.put(rpcInvocation.getUuid(), rpcInvocation);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}
