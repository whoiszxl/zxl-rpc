package com.whoiszxl.rpc.core.proxy.jdk;

import com.whoiszxl.rpc.core.client.RpcReferenceWrapper;
import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * JDK实际代理调用处理器
 */
public class JDKClientInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private RpcReferenceWrapper rpcReferenceWrapper;

    private int timeout = 3000;

    public JDKClientInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //封装rpc调用，将参数、方法名，服务名封装
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttachments());
        //设置uuid，通过uuid将请求与返回进行匹配
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        //将请求包封装到队列里，发送线程会消费这个队列进行发送
        RpcClientCache.SEND_QUEUE.add(rpcInvocation);

        //如果是异步请求，就不需要判断结果了
        if(rpcReferenceWrapper.isAsync()) {
            return null;
        }

        RpcClientCache.RESPONSE_CACHES.put(rpcInvocation.getUuid(), OBJECT);
        //死循环在结果缓存里获取，拿到结果直接返回，3秒超时
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < timeout) {
            Object object = RpcClientCache.RESPONSE_CACHES.get(rpcInvocation.getUuid());
            if(object instanceof RpcInvocation) {
                return ((RpcInvocation) object).getResponse();
            }
        }

        throw new TimeoutException("请求超时");
    }
}
