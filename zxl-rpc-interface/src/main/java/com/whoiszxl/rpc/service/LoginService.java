package com.whoiszxl.rpc.service;

public interface LoginService {

    /**
     * 登录接口
     * @param username 用户名
     * @param password 密码
     * @return
     */
    String login(String username, String password);

}
