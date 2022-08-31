package com.whoiszxl.rpc.core.registy;

import com.whoiszxl.rpc.core.registy.zk.ProviderNodeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册中心专用URL
 */
public class RegURL {

    /**
     * 服务应用名称
     */
    private String applicationName;

    /**
     * 服务名称，全限定名称
     */
    private String serviceName;


    private Map<String, String> parameters = new HashMap<>();

    public void addParameter(String key, String value) {
        this.parameters.putIfAbsent(key, value);
    }

    public static String buildProviderUrlStr(RegURL url) {
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        return url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis();
    }

    public static String buildConsumerUrlStr(RegURL url) {
        String host = url.getParameters().get("host");
        return url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis();
    }

    public static ProviderNodeInfo buildUrlFromUrlStr(String providerNodeStr) {
        String[] items = providerNodeStr.split("/");
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setServiceName(items[2]);
        providerNodeInfo.setAddress(items[4]);
        return providerNodeInfo;
    }




    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
