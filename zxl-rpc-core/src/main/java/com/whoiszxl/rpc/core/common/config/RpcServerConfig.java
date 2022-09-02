package com.whoiszxl.rpc.core.common.config;

/**
 * rpc server config
 */
public class RpcServerConfig {

    private Integer serverPort;

    private String registerAddr;

    private String applicationName;

    /**
     * 服务端序列化方式 jdk,kryo
     */
    private String serverSerialize;

    public String getServerSerialize() {
        return serverSerialize;
    }

    public void setServerSerialize(String serverSerialize) {
        this.serverSerialize = serverSerialize;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
