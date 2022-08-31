package com.whoiszxl.rpc.core.common.event;

public class RpcUpdateEvent implements RpcEvent{

    private Object data;

    public RpcUpdateEvent(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public RpcEvent setData(Object data) {
        return null;
    }
}
