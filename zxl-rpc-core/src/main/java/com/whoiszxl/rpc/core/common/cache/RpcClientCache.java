package com.whoiszxl.rpc.core.common.cache;

import com.whoiszxl.rpc.core.common.event.data.ChannelFuturePollingRef;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.router.IRouter;
import com.whoiszxl.rpc.core.serialize.SerializeFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc客户端缓存
 */
public class RpcClientCache {

    /**
     * 客户端请求发送队列，单独线程进行消费处理
     */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<RpcInvocation>(128);

    /**
     * 服务端返回的响应实体信息
     */
    public static Map<String,Object> RESPONSE_CACHES = new ConcurrentHashMap<>();

    /**
     * 订阅服务列表
     */
    public static List<RegURL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();


    /**
     * 每次进行远程调用的时候都是从这里面去选择服务提供者
     */
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();


    public static Map<String, Map<String, String>> URL_MAP = new ConcurrentHashMap<>();


    public static Set<String> SERVER_ADDRESS = new HashSet<>();



    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();

    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();


    public static IRouter IROUTER;

    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;

}
