package com.whoiszxl.rpc.core.router;

import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.registy.RegURL;

public interface IRouter {


    /**
     * 刷新路由数组
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 选择连接通道
     * @param selector
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重
     * @param regURL
     */
    void updateWeight(RegURL regURL);
}
