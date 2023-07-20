package com.dlut.community.controller;

import com.dlut.community.Event.EventProducer;
import com.dlut.community.Service.LikeService;
import com.dlut.community.pojo.Event;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.CommunityUtil;
import com.dlut.community.util.HostHolder;
import com.dlut.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        //获取点赞用户
        User user = hostHolder.getUser(); //这里不用再判断user是否登录，以后会统一做安全处理
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //点赞数量
        long count = likeService.findEntityLikeCount(entityType, entityId);
        //用户点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //返回结果用map封装起来
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", count);
        map.put("likeStatus", likeStatus);

        //触发点赞事件
        //注意点赞事件有两种功能，一个是点赞，一个是取消赞，需作出判断，取消赞的时候不通知
        if(likeStatus == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);

            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        if(entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }


        return CommunityUtil.getJSONString(0, null, map);
    }
}
