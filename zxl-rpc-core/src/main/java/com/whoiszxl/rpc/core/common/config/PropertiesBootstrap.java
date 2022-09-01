package com.whoiszxl.rpc.core.common.config;

import java.io.IOException;

public class PropertiesBootstrap {
    
    private volatile boolean configIsReady;

    public static final String SERVER_PORT = "zxl-rpc.serverPort";
    public static final String REGISTER_ADDRESS = "zxl-rpc.registerAddr";
    public static final String APPLICATION_NAME = "zxl-rpc.applicationName";
    public static final String PROXY_TYPE = "zxl-rpc.proxyType";
    public static final String ROUTER_STRATEGY = "zxl-rpc.routerStrategy";

    public static RpcServerConfig loadServerConfigFromLocal() {
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadServerConfigFromLocal fail,e is {}", e);
        }
        RpcServerConfig serverConfig = new RpcServerConfig();
        serverConfig.setServerPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        return serverConfig;
    }

    public static RpcClientConfig loadClientConfigFromLocal(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadClientConfigFromLocal fail,e is {}", e);
        }
        RpcClientConfig clientConfig = new RpcClientConfig();
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        clientConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStr(PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStr(ROUTER_STRATEGY));
        return clientConfig;
    }

}
