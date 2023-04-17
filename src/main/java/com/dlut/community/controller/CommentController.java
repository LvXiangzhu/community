package com.dlut.community.controller;

import com.dlut.community.Event.EventProducer;
import com.dlut.community.Service.CommentService;
import com.dlut.community.Service.DiscussPostService;
import com.dlut.community.pojo.Comment;
import com.dlut.community.pojo.DiscussPost;
import com.dlut.community.pojo.Event;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.HostHolder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    /*
    * 增加评论
    * */
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    //页面需要提交什么：帖子内容、实体类型、实体id, 所以参数里写一个Comment。
    // 页面会把comment里的帖子内容、实体类型、实体id信息直接传过来，其余的属性在方法里设置
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //没登录也没关系，因为后序会做统一的异常处理和权限认证
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(post.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment comment1 = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(comment1.getUserId());
        }

        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
