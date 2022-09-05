package com.whoiszxl.rpc.core.dispatcher;

import com.whoiszxl.rpc.core.common.cache.RpcServerCache;
import com.whoiszxl.rpc.core.common.pack.RpcInvocation;
import com.whoiszxl.rpc.core.common.pack.RpcProtocol;
import com.whoiszxl.rpc.core.server.ServerChannelReadData;

import java.lang.reflect.Method;
import java.util.concurrent.*;

public class ServerChannelDispatcher {

    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    private ExecutorService executorService;

    public ServerChannelDispatcher() {}

    public void init(int queueSize, int bizThreadNums) {
        RPC_DATA_QUEUE = new ArrayBlockingQueue<ServerChannelReadData>(queueSize);
        executorService = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());
    }

    public void add(ServerChannelReadData serverChannelReadData) {
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }


    public void startDataConsume() {
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }

    class ServerJobCoreHandle implements Runnable {

        @Override
        public void run() {
            while (true) {

                try {
                    //从阻塞队列中获取到客户端发送过来的请求
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                    RpcInvocation rpcInvocation = RpcServerCache.SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //获取到请求中的实际参数
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation = RpcServerCache.SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);

                                //执行过滤链
                                RpcServerCache.SERVER_FILTER_CHAIN.doFilter(rpcInvocation);

                                //获取到客户端需要调用的服务对象
                                Object aimObject = RpcServerCache.PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();

                                Object result = null;
                                for (Method method : methods) {
                                    //排除不匹配的方法
                                    if(!method.getName().equals(rpcInvocation.getTargetMethod())) {
                                        continue;
                                    }

                                    //区分有参返回与无参返回调用
                                    if(method.getReturnType().equals(Void.TYPE)) {
                                        try {
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        }catch (Exception e) {
                                            rpcInvocation.setE(e);
                                        }
                                    }else {
                                        try {
                                            result = method.invoke(aimObject, rpcInvocation.getArgs());
                                        }catch (Exception e) {
                                            rpcInvocation.setE(e);
                                        }
                                    }
                                    break;
                                }

                                rpcInvocation.setResponse(result);
                                RpcProtocol response = new RpcProtocol(RpcServerCache.SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(response);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

}
