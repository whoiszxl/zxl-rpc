package com.whoiszxl.rpc.core.spi.jdk;

import java.util.Iterator;
import java.util.ServiceLoader;

public class TestSpiDemo {

    public static void doTest(ISpiTest iSpiTest) {
        System.out.println("begin");
        iSpiTest.doTest();
        System.out.println("end");
    }

    public static void main(String[] args) {
        ServiceLoader<ISpiTest>  serviceLoader = ServiceLoader.load(ISpiTest.class);
        Iterator<ISpiTest> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            ISpiTest iSpiTest = iterator.next();
            TestSpiDemo.doTest(iSpiTest);
        }
    }

}
