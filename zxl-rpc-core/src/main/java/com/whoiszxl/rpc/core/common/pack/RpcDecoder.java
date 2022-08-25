package com.whoiszxl.rpc.core.common.pack;

import com.whoiszxl.rpc.core.common.constants.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= BASE_LENGTH) {

            if(in.readableBytes() > MAX_LENGTH) {
                in.skipBytes(in.readableBytes());
            }

            int beginReader;
            while(true) {
                beginReader = in.readerIndex();
                in.markReaderIndex();

                if(in.readShort() == RpcConstants.MAGIC_NUMBER) {
                    break;
                }else {
                    ctx.close();
                    return;
                }
            }

            //如果读取到的包长度小于包定义的长度，则说明数据包不完整，需要重置索引
            int contentLength = in.readInt();
            if(in.readableBytes() < contentLength) {
                in.readerIndex(beginReader);
                return;
            }

            byte[] data = new byte[contentLength];
            in.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            out.add(rpcProtocol);

        }

    }
}
