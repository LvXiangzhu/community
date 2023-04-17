package com.dlut.community.controller;

import com.dlut.community.Service.FollowService;
import com.dlut.community.Service.LikeService;
import com.dlut.community.Service.UserService;
import com.dlut.community.annotation.LoginRequired;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.CommunityUtil;
import com.dlut.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

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

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /*
    * 获取用户设置页面
    * */
    @LoginRequired
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
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headImg, Model model) {
        //如果上传文件问空，返回错误信息并刷新setting页面
        if(headImg == null) {
            model.addAttribute("error", "您还没有上传图片！");
            return "/site/setting";
        }

        String filename = headImg.getOriginalFilename(); //获取图片文件名

        //若文件没有后缀名，返回错误信息并刷新setting页面
        if(!filename.contains(".")) {
            model.addAttribute("error", "您上传的文件格式有误！");
            return "/site/setting";
        }
        String suffix = filename.substring(filename.lastIndexOf("."));//获取图片文件后缀(上面三行是后加的，如果没有的话这句话会报错)
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

    /*
    * 显示头像
    * */
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        //服务器存放路径
        filename = uploadPath + "/" + filename;
        // 文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatepassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword, String newPassword, String reapeatNewPassword) {
        User user = hostHolder.getUser();
        if(user == null) {
            throw new RuntimeException("找不到该用户，系统异常！");
        }
        //密码别忘了加盐再加密
        String oldPassword1 = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword1)) {
            model.addAttribute("oldPasswordMsg", "原密码不正确！");
            return "/site/setting";
        }
        if(!newPassword.equals(reapeatNewPassword)) {
            model.addAttribute("reapeatPasswordMsg", "两次输入不一致！");
            return "/site/setting";
        }

        if(newPassword.equals(oldPassword)) {
            model.addAttribute("newPasswordMsg", "新密码和原密码相同！");
            return "/site/setting";
        }
        //如果均正确，修改密码
        String newpsd = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), newpsd);

        return "redirect:/index";
    }

    /*
    * 查看主页
    * userId : 被查看主页的用户id
    * */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        boolean hasFollowed = false;
        //判断用户是否登录，没登录就不会显示已关注状态
        if(hostHolder.getUser() != null) {
            hasFollowed  = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        model.addAttribute("loginUser", hostHolder.getUser());

        return "/site/profile";
    }
}
