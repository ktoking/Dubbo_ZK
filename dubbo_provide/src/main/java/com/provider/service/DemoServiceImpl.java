package com.provider.service;

import com.api.service.DemoService;

public class DemoServiceImpl implements DemoService {
    public String sayHello(String name) {
        return "Hello "+name;
    }

    public String myFun(String no) {
        return no+"提供者本机服务器提供";
    }
}
