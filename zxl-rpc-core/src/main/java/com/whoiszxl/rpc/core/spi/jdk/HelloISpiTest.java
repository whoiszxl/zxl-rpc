package com.whoiszxl.rpc.core.spi.jdk;

public class HelloISpiTest implements ISpiTest{


    @Override
    public void doTest() {
        System.out.println("hello");
    }
}
