package com.whoiszxl.rpc.core.common.cache;

import com.whoiszxl.rpc.core.common.config.RpcServerConfig;
import com.whoiszxl.rpc.core.filter.server.ServerFilterChain;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.RegistryService;
import com.whoiszxl.rpc.core.serialize.SerializeFactory;
import com.whoiszxl.rpc.core.server.ServiceWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc服务端缓存
 */
public class RpcServerCache {

    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<>();

    public static final Set<RegURL> PROVIDER_URL_SET = new HashSet<>();

    public static RegistryService REGISTRY_SERVICE;

    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

    public static ServerFilterChain SERVER_FILTER_CHAIN;

    public static RpcServerConfig SERVER_CONFIG;

    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();


}
