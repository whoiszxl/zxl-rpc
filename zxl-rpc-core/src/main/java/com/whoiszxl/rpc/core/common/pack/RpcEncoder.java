package com.whoiszxl.rpc.core.common.pack;

import com.whoiszxl.rpc.core.common.constants.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * rpc编码器
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol, ByteBuf byteBuf) throws Exception {
        byteBuf.writeShort(rpcProtocol.getMagicNumber());
        byteBuf.writeInt(rpcProtocol.getContentLength());
        byteBuf.writeBytes(rpcProtocol.getContent());
        byteBuf.writeBytes(RpcConstants.DEFAULT_DECODE_CHAR.getBytes());
    }
}
