package com.dlut.community.controller;

import com.dlut.community.Service.DiscussPostService;
import com.dlut.community.Service.UserService;
import com.dlut.community.pojo.DiscussPost;
import com.dlut.community.pojo.Page;
import com.dlut.community.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    UserService userService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        // 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.

        //设置数据总行数
        page.setRows(discussPostService.findDiscussPostRows(0));
        //设置页面路径
        page.setPath("/index");

        List<DiscussPost> discussPost = discussPostService.findDiscussPost(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(discussPost != null) {
            for (DiscussPost post : discussPost) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(Integer.parseInt(post.getUserId()));
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("DiscussPosts", discussPosts);
        return "/index";
    }
}

