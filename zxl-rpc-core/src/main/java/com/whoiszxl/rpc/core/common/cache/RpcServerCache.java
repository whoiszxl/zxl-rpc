package com.whoiszxl.rpc.core.common.cache;

import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.RegistryService;
import com.whoiszxl.rpc.core.serialize.SerializeFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * rpc服务端缓存
 */
public class RpcServerCache {

    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<>();

    public static final Set<RegURL> PROVIDER_URL_SET = new HashSet<>();

    public static RegistryService REGISTRY_SERVICE;

    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

}
