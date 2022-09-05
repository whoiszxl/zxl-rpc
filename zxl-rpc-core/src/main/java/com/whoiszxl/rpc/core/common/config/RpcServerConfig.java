package com.whoiszxl.rpc.core.common.config;

/**
 * rpc server config
 */
public class RpcServerConfig {

    private Integer serverPort;

    private String registerAddr;

    private String registerType;

    private String applicationName;

    /**
     * 服务端序列化方式 jdk,kryo
     */
    private String serverSerialize;

    /**
     * 服务端处理请求的线程数
     */
    private Integer serverBizThreadNums;

    /**
     * 服务端接收客户端请求的队列的大小
     */
    private Integer serverQueueSize;

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

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public Integer getServerBizThreadNums() {
        return serverBizThreadNums;
    }

    public void setServerBizThreadNums(Integer serverBizThreadNums) {
        this.serverBizThreadNums = serverBizThreadNums;
    }

    public Integer getServerQueueSize() {
        return serverQueueSize;
    }

    public void setServerQueueSize(Integer serverQueueSize) {
        this.serverQueueSize = serverQueueSize;
    }
}
