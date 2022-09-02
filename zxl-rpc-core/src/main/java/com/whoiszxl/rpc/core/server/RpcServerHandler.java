package com.whoiszxl.rpc.core.server;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.common.pack.RpcProtocol;
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
        //经过rpc编码器处理后，数据包直接是RpcProtocol的格式了
        RpcProtocol rpcProtocol = (RpcProtocol) msg;

        //从自定义协议中获取到需要调用的服务名与方法，通过自定义序列化方式进行反序列化，从缓存中通过服务名拿到实例对象
        RpcInvocation rpcInvocation =RpcServerCache.SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);

        //执行责任链
        RpcServerCache.SERVER_FILTER_CHAIN.doFilter(rpcInvocation);

        Object aimObject = RpcServerCache.PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
        Method[] methods = aimObject.getClass().getDeclaredMethods();

        //遍历方法，找到请求中需要调用的方法，如果有返回值，将response封装回协议中再写回
        Object result = null;
        for (Method method : methods) {
            if(method.getName().equals(rpcInvocation.getTargetMethod())) {
                if(method.getReturnType().equals(Void.TYPE)) {
                    method.invoke(aimObject, rpcInvocation.getArgs());
                }else {
                    result = method.invoke(aimObject, rpcInvocation.getArgs());
                }
                break;
            }
        }

        rpcInvocation.setResponse(result);
        RpcProtocol responseRpcProtocol = new RpcProtocol(RpcServerCache.SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
        ctx.writeAndFlush(responseRpcProtocol);
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
