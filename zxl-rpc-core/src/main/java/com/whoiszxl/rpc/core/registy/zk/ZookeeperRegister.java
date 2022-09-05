package com.whoiszxl.rpc.core.registy.zk;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.event.RpcEvent;
import com.whoiszxl.rpc.core.common.event.RpcListenerLoader;
import com.whoiszxl.rpc.core.common.event.RpcNodeChangeEvent;
import com.whoiszxl.rpc.core.common.event.RpcUpdateEvent;
import com.whoiszxl.rpc.core.common.event.data.URLChangeWrapper;
import com.whoiszxl.rpc.core.registy.RegURL;
import com.whoiszxl.rpc.core.registy.RegistryService;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private AbstractZookeeperClient zkClient;

    private String ROOT = "/zxl-rpc";

    private String getProviderPath(RegURL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(RegURL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host")+":";
    }

    public ZookeeperRegister() {
        String registryAddr = RpcClientCache.CLIENT_CONFIG != null ? RpcClientCache.CLIENT_CONFIG.getRegisterAddr() : RpcServerCache.SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
    }

    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    @Override
    public List<String> getProviderIps(String serviceName) {
        return this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
    }

    /**
     * 注册服务到zk上
     * @param url 服务url
     */
    @Override
    public void register(RegURL url) {
        //如果根节点不存在，则创建
        checkRootNode();

        String urlStr = RegURL.buildProviderUrlStr(url);
        String providerPath = getProviderPath(url);

        if (zkClient.existNode(providerPath)) {
            zkClient.deleteNode(providerPath);
        }
        zkClient.createTemporaryData(providerPath, urlStr);

        super.register(url);
    }

    @Override
    public void unRegister(RegURL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(RegURL url) {
        checkRootNode();
        String urlStr = RegURL.buildConsumerUrlStr(url);
        String consumerPath = getConsumerPath(url);

        if(zkClient.existNode(consumerPath)) {
            zkClient.deleteNode(consumerPath);
        }
        zkClient.createTemporaryData(consumerPath, urlStr);
        super.subscribe(url);
    }

    @Override
    public void doUnSubscribe(RegURL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }

    @Override
    public void doAfterSubscribe(RegURL url) {
        String servicePath = url.getParameters().get("servicePath");
        String newServerNodePath = ROOT + "/" + servicePath;
        watchChildNodeData(newServerNodePath);

        String providerIpsStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpsStrJson, List.class);

        for (String providerIp : providerIpList) {
            this.watchNodeDataChange(ROOT + "/" + servicePath + "/" + providerIp);
        }

    }

    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                String nodeData = zkClient.getNodeData(path);
                nodeData = nodeData.replace(";", "/");

                ProviderNodeInfo providerNodeInfo = RegURL.buildUrlFromUrlStr(nodeData);

                RpcEvent rpcEvent = new RpcNodeChangeEvent(providerNodeInfo);
                RpcListenerLoader.sendEvent(rpcEvent);
                watchNodeDataChange(newServerNodePath);
            }
        });
    }

    public void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                List<String> childrenDataList = zkClient.getChildrenData(path);
                URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
                urlChangeWrapper.setProviderUrl(childrenDataList);
                urlChangeWrapper.setServiceName(path.split("/")[2]);

                RpcEvent rpcEvent = new RpcUpdateEvent(urlChangeWrapper);
                RpcListenerLoader.sendEvent(rpcEvent);
                watchChildNodeData(path);

            }
        });
    }

    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>();

        for (String addr : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + addr);
            result.put(addr, childData);
        }
        return result;
    }

    @Override
    public void doBeforeSubscribe(RegURL url) {

    }



    /**
     * 如果根节点不存在，则创建
     */
    private void checkRootNode() {
        if(!this.zkClient.existNode(ROOT)) {
            this.zkClient.createPersistentData(ROOT, "");
        }
    }
}
