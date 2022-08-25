package com.whoiszxl.rpc.core.common.config;

/**
 * rpc客户端配置
 */
public class RpcClientConfig {

    private Integer port;

    private String serverHost;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }
}
