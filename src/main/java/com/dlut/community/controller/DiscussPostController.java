package com.dlut.community.controller;

import com.dlut.community.Event.EventProducer;
import com.dlut.community.Service.CommentService;
import com.dlut.community.Service.DiscussPostService;
import com.dlut.community.Service.LikeService;
import com.dlut.community.Service.UserService;
import com.dlut.community.pojo.*;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.CommunityUtil;
import com.dlut.community.util.HostHolder;
import com.dlut.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    RedisTemplate redisTemplate;

    /*
    * 方法功能：发布帖子
    * 参数：标题，内容
    * 返回值：Json字符串
    * */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setType(0);
        post.setStatus(0);
        post.setCommentCount(0);
        post.setScore(0);
        discussPostService.addDiscussPost(post);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        //报错的情况将来同一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /*
    * 方法功能：查询帖子详情
    * 参数：帖子Id，模板model
    * 返回值：路径
    * */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //显示赞数
        long postLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("postLikeCount", postLikeCount);

        //赞的状态
        //注意确保用户处于登录状态！！！
        int postLikeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("postLikeStatus", postLikeStatus);

        //需要处理用户
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //查到评论后，还需要显示用户名
        //所以用Map把对应的用户和评论存起来
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null) {
            for(Comment comment : commentList) {
                //评论Vo
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //赞数
                commentVo.put("commentLikeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
                //赞的状态
                int commentLikeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("commentLikeStatus", commentLikeStatus);
                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE); //回复就不分页了
                //回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for(Comment reply : replyList) {
                        Map<String, Object> replytVo = new HashMap<>();
                        //回复
                        replytVo.put("reply", reply);
                        //作者
                        replytVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replytVo.put("target", target);
                        //赞数
                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replytVo.put("replyLikeCount", replyLikeCount);
                        //赞的状态
                        int replyLikeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        commentVo.put("replyLikeStatus", replyLikeStatus);

                        replyVoList.add(replytVo);
                    }
                }
                commentVo.put("replies", replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail";
    }

    /*
    * 置顶
    * */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 有ES时，需触发发帖事件
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                .setUserId(hostHolder.getUser().getId())
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityId(id); //如果不设置自增主键的话这里就会有问题
//        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                .setUserId(hostHolder.getUser().getId())
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityId(id);
//        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
//        Event event = new Event()
//                .setTopic(TOPIC_DELETE)
//                .setUserId(hostHolder.getUser().getId())
//                .setEntityType(ENTITY_TYPE_POST)
//                .setEntityId(id);
//        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
