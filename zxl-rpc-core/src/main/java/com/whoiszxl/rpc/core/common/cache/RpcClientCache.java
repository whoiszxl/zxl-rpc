package com.whoiszxl.rpc.core.common.cache;

import com.whoiszxl.rpc.core.common.pack.RpcInvocation;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc客户端缓存
 */
public class RpcClientCache {

    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<RpcInvocation>(128);
    public static Map<String,Object> RESPONSE_CACHES = new ConcurrentHashMap<>();
}
