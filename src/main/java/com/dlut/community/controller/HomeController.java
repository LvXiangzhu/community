package com.dlut.community.controller;

import com.dlut.community.Service.DiscussPostService;
import com.dlut.community.Service.LikeService;
import com.dlut.community.Service.UserService;
import com.dlut.community.pojo.DiscussPost;
import com.dlut.community.pojo.Page;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               //首次访问时没有参数，因此需要加@RequestParam注解传一个默认值0，否则orderMode接收不到参数就会报错
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.

        //设置数据总行数
        page.setRows(discussPostService.findDiscussPostRows(0));
        //设置页面路径
        //重构orderMode时需要拼路径
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> discussPost = discussPostService
                .findDiscussPost(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(discussPost != null) {
            for (DiscussPost post : discussPost) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                //新增显示赞数的逻辑
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("DiscussPosts", discussPosts);
        //重构时模板要用orderMode
        model.addAttribute("orderMode", orderMode);

        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "error/404";
    }
}

