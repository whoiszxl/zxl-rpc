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
     * 代理类型：jdk javassist
     */
    private String proxyType;

    /**
     * 路由负载均衡策略：random(随机) rotate(轮询)
     */
    private String routerStrategy;

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
}
