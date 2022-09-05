package com.whoiszxl.rpc.core.common.pack;

import com.whoiszxl.rpc.core.common.constants.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.whoiszxl.rpc.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * rpc解码器
 *
 * 需要考虑到粘包、拆包的现象
 * 出现原因：因为TCP发消息的时候是有缓冲区的，当消息远小于缓冲区的时候，消息会等待其他消息一起发送
 *
 */
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议开头部分长度，魔法值长度
     */
    public static final int BASE_LENGTH = 4;

    /**
     * 接收包的最大长度
     */
    public static final int MAX_LENGTH = 1000;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)  {
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            if (!(byteBuf.readShort() == MAGIC_NUMBER)) {
                ctx.close();
                return;
            }
            int length = byteBuf.readInt();
            if (byteBuf.readableBytes() < length) {
                //数据包有异常
                ctx.close();
                return;
            }
            byte[] body = new byte[length];
            byteBuf.readBytes(body);
            RpcProtocol rpcProtocol = new RpcProtocol(body);
            out.add(rpcProtocol);
        }
    }
}
