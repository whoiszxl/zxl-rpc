package com.whoiszxl.rpc.core.common.pack;

import com.whoiszxl.rpc.core.common.constants.RpcConstants;

import java.io.Serializable;

/**
 * 自定义RPC协议
 */
public class RpcProtocol implements Serializable {

    /**
     * 魔法数，检测RPC调用时传递的包是否复合规则
     */
    private short magicNumber = RpcConstants.MAGIC_NUMBER;

    /**
     * RPC传输的数据长度
     */
    private int contentLength;

    /**
     * RPC实际传输的数据内容
     */
    private byte[] content;


    public RpcProtocol(byte[] content) {
        this.contentLength = content.length;
        this.content = content;
    }

    public short getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(short magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
