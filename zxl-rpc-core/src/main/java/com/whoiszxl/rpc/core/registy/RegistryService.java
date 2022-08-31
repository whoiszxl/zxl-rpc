package com.whoiszxl.rpc.core.registy;

/**
 * 注册中心服务
 */
public interface RegistryService {


    /**
     * 注册服务
     * @param url 服务url
     */
    void register(RegURL url);

    /**
     * 下线服务
     * @param url 服务url
     */
    void unRegister(RegURL url);

    /**
     * 订阅服务，客户端启动时调用，从注册中心获取现有服务提供者地址
     * @param url 服务url
     */
    void subscribe(RegURL url);


    /**
     * 取消订阅服务
     * @param url 服务url
     */
    void doUnSubscribe(RegURL url);
}
