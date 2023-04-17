package com.dlut.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.dlut.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "hello spring boot!";
    }

    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String Ajax(String name, String age) {
        System.out.println(name);
        System.out.println(age);

        return CommunityUtil.getJSONString(0, "操作成功！");
    }
}
