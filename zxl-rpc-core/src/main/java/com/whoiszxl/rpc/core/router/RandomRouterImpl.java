package com.whoiszxl.rpc.core.router;

import com.whoiszxl.rpc.core.common.cache.RpcClientCache;
import com.whoiszxl.rpc.core.common.event.data.ChannelFutureWrapper;
import com.whoiszxl.rpc.core.registy.RegURL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 随机路由实现
 */
public class RandomRouterImpl implements IRouter {


    @Override
    public void refreshRouterArr(Selector selector) {
        List<ChannelFutureWrapper> channelFutureWrapperList = RpcClientCache.CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrapperList.size()];

        int[] result = createRandomIndex(arr.length);
        for (int i = 0; i < result.length; i++) {
            arr[i] = channelFutureWrapperList.get(result[i]);
        }

        RpcClientCache.SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
    }

    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return RpcClientCache.CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    @Override
    public void updateWeight(RegURL regURL) {
        List<ChannelFutureWrapper> channelFutureWrapperList = RpcClientCache.CONNECT_MAP.get(regURL.getServiceName());
        Integer[] weightArr = createWeightArr(channelFutureWrapperList);
        Integer[] finalArr = createRandomArr(weightArr);
        ChannelFutureWrapper[] finalChannelFutureWrapperList = new ChannelFutureWrapper[finalArr.length];

        for (int i = 0; i < finalArr.length; i++) {
            finalChannelFutureWrapperList[i] = channelFutureWrapperList.get(i);
        }

        RpcClientCache.SERVICE_ROUTER_MAP.put(regURL.getServiceName(), finalChannelFutureWrapperList);
    }


    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrapperList) {
        List<Integer> weightArr = new ArrayList<>();
        for (int k = 0; k < channelFutureWrapperList.size(); k++) {
            Integer weight = channelFutureWrapperList.get(k).getWeight();
            int c = weight / 100;
            for (int i = 0; i < c; i++) {
                weightArr.add(k);
            }
        }
        Integer[] arr = new Integer[weightArr.size()];
        return weightArr.toArray(arr);
    }


    private static Integer[] createRandomArr(Integer[] arr) {
        //获取数组的长度
        int total = arr.length;
        Random ra = new Random();
        for (int i = 0; i < total; i++) {
            //创建随机数
            int j = ra.nextInt(total);

            //如果随机数和数组下标一致，就跳出
            if (i == j) {
                continue;
            }

            //不一致，就将下标为i的和j的互换
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }


    private int[] createRandomIndex(int len) {
        //创建对应长度的数组
        int[] arrInt = new int[len];

        Random ra = new Random();

        //将数组中的每个值都赋值为-1
        Arrays.fill(arrInt, -1);

        //遍历，将随机数填充到数组中
        int index = 0;
        while (index < arrInt.length) {
            int num = ra.nextInt(len);
            //如果数组中不包含这个元素则赋值给数组
            if (!contains(arrInt, num)) {
                arrInt[index++] = num;
            }
        }
        return arrInt;
    }

    public boolean contains(int[] arr, int key) {
        for (int j : arr) {
            if (j == key) {
                return true;
            }
        }
        return false;
    }
}
