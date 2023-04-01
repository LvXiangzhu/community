package com.dlut.community.controller;

import com.dlut.community.Service.UserService;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityUtil;
import com.dlut.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    //域名
    @Value("${community.path.domain}")
    private String domain;

    //项目访问路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    //上传路径
    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    //取当前用户
    @Autowired
    private HostHolder hostHolder;

    /*
    * 获取用户设置页面
    * */
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /*
    * 方法功能：上传头像
    * 方法参数：
    *  MultipartFile：用于接收头像文件
    *  Model：用于保存错误信息给前端
    * 返回值：路径
    * */
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headImg, Model model) {
        //如果上传文件问空，返回错误信息并刷新setting页面
        if(headImg == null) {
            model.addAttribute("error", "您还没有上传图片！");
            return "/site/setting";
        }

        String filename = headImg.getOriginalFilename(); //获取图片文件名
        String suffix = filename.substring(filename.lastIndexOf("."));//获取图片文件后缀

        //若文件没有后缀名，返回错误信息并刷新setting页面
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "您上传的文件格式有误！");
            return "/site/setting";
        }

        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败！" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！" + e);
        }

        //更新当前用户头像路径
        //http://localhost:8080/community/user/header/xxx.png
        String headUrl = domain + contextPath + "/user/header/" + filename;
        User user = hostHolder.getUser();
        userService.updateHead(user.getId(), headUrl);
        return "redirect:/index";
    }
}
