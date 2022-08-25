package com.whoiszxl.rpc.core.common.pack;

/**
 * RPC调用需要使用到的参数封装
 */
public class RpcInvocation {

    /**
     * 调用的目标方法
     */
    private String targetMethod;

    /**
     * 调用的目标服务
     */
    private String targetServiceName;

    /**
     * 调用的目标方法的参数信息
     */
    private Object[] args;

    /**
     * 请求方传递过来的uuid，用于请求与响应的匹配
     */
    private String uuid;

    /**
     * 接口响应参数
     */
    private Object response;


    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
