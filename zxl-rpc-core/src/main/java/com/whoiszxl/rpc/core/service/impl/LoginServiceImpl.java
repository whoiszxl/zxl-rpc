package com.whoiszxl.rpc.core.service.impl;

import com.whoiszxl.rpc.service.LoginService;

public class LoginServiceImpl implements LoginService {
    @Override
    public String login(String username, String password) {
        return username + "登录成功";
    }
}
