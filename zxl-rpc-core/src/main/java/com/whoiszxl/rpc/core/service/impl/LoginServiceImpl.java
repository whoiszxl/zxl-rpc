package com.whoiszxl.rpc.core.service.impl;

import com.whoiszxl.rpc.service.LoginService;

public class LoginServiceImpl implements LoginService {
    @Override
    public String login(String username, String password) {
        System.out.println(username + "登录成功");
        int i = 1/0;
        return username + "登录成功";
    }
}
