package com.whoiszxl.rpc.core.common.config;

/**
 * rpc客户端配置
 */
public class RpcClientConfig {

    /**
     * 客户端应用名称
     */
    private String applicationName;

    /**
     * 注册中心地址
     */
    private String registerAddr;

    /**
     * 注册中心类型
     */
    private String registerType;

    /**
     * 代理类型：jdk javassist
     */
    private String proxyType;

    /**
     * 路由负载均衡策略：random(随机) rotate(轮询)
     */
    private String routerStrategy;

    /**
     * 客户端序列化方式 jdk,kryo
     */
    private String clientSerialize;


    public String getClientSerialize() {
        return clientSerialize;
    }

    public void setClientSerialize(String clientSerialize) {
        this.clientSerialize = clientSerialize;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getRouterStrategy() {
        return routerStrategy;
    }

    public void setRouterStrategy(String routerStrategy) {
        this.routerStrategy = routerStrategy;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }
}
