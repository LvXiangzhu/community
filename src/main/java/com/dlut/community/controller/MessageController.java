package com.dlut.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.dlut.community.Service.MessageService;
import com.dlut.community.Service.UserService;
import com.dlut.community.dao.MessageMapper;
import com.dlut.community.pojo.Message;
import com.dlut.community.pojo.Page;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.CommunityUtil;
import com.dlut.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();
        //设置数据总行数
        page.setRows(messageService.findConversationCount(user.getId()));
        //设置页面路径（好像是给页面模板用的）
        page.setPath("/message/letter/list");
        page.setLimit(5);

        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());

        //需要显示的内容：最新的会话消息、会话上的未读消息、每个会话共多少条私信
        //这些都要装到conversationMaps里
        List<Map<String, Object>> conversationMaps = new ArrayList<>();
        if(conversations != null) {
            for (Message conversation : conversations) {
                Map<String, Object> map = new HashMap<>();
                //最新的会话消息
                map.put("conversation", conversation);
                //会话上的未读消息
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
                //每个会话共多少条私信
                map.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
                //对方用户的头像，需要首先判断该条消息的对方用户id是from_id还是to_id
                int targetId;
                if(user.getId() == conversation.getFromId()) {
                    targetId = conversation.getToId();
                }else{
                    targetId = conversation.getFromId();
                }
                map.put("target", userService.findUserById(targetId));

                conversationMaps.add(map);
            }
        }
        model.addAttribute("conversationMaps", conversationMaps);

        //总未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //查询系统通知总未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    //私信详情
    //因为是点进一个会话进入详情界面，所以需要知道点的是哪个会话，需要传过来会话id
    @RequestMapping(value = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {

        //设置页面信息
        page.setLimit(5);
        page.setPath("/message/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letters = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());

        //设置消息为已读(别忘了判空)
        List<Integer> ids = getLetterIds(letters);
        if(!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        //除了信息内容外，需要补充头像和用户名
        //谁是发信人，就补充谁，所以补充的是from用户
        //所以还是需要建一个map
        List<Map<String, Object>> letterMaps = new ArrayList<>();
        if(letters != null) {
            for (Message letter : letters) {

                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                letterMaps.add(map);
            }
        }
        model.addAttribute("letterMaps", letterMaps);

        //来自xxx的私信
        model.addAttribute("target", getLetterTarget(conversationId));
        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if(letterList != null) {
            for (Message message : letterList) {
                //需要先看当前用户是否是接收者，接收者才有读的状态 //消息处于未读的状态
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                   ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }

    /*
    * 发送私信
    * */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody   //异步请求，用json
    public String sendLetter(String toName, String content) {

        //查找到对话方
        User target = userService.findUserByName(toName);
        //别忘了这个名字可能不存在，需要返回提示信息
        if(target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        Message message = new Message();
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setCreateTime(new Date());
        message.setStatus(0);
        message.setToId(target.getId());
        String conversationId = hostHolder.getUser().getId() < target.getId() ? hostHolder.getUser().getId() +"_"+ target.getId() : target.getId() + "_" + hostHolder.getUser().getId();
        message.setConversationId(conversationId);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    /*
    * 系统通知
    * */
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        //查询评论类通知
        Message comment = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);

        if(comment != null) {
            Map<String, Object> commentVo = new HashMap<>();
            commentVo.put("comment", comment);

            //获取数据库中content里的内容
            // 里面的双引号转义字符要转义回来
            String content = HtmlUtils.htmlUnescape(comment.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            Set<Map.Entry<String, Object>> entries = data.entrySet();
            for(Map.Entry<String, Object> entry : entries) {
                commentVo.put(entry.getKey(), entry.getValue());
            }
            commentVo.put("user", userService.findUserById((Integer) data.get("userId")));

            int commentCount = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            commentVo.put("count", commentCount);

            int commentUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            commentVo.put("unread", commentUnreadCount);

            model.addAttribute("commentNotice", commentVo);
        }



        //查询点赞类通知
        Message like = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

        if(like != null) {
            Map<String, Object> likeVo = new HashMap<>();
            likeVo.put("like", like);

            //获取数据库中content里的内容
            // 里面的双引号转义字符要转义回来
            String content = HtmlUtils.htmlUnescape(like.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            Set<Map.Entry<String, Object>> entries = data.entrySet();
            for(Map.Entry<String, Object> entry : entries) {
                likeVo.put(entry.getKey(), entry.getValue());
            }
            likeVo.put("user", userService.findUserById((Integer) data.get("userId")));

            int likeCount = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            likeVo.put("count", likeCount);

            int likeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            likeVo.put("unread", likeUnreadCount);

            model.addAttribute("likeNotice", likeVo);
        }


        //查询关注类通知
        Message follow = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);

        if(follow != null) {
            Map<String, Object> followVo = new HashMap<>();
            followVo.put("follow", follow);

            //获取数据库中content里的内容
            // 里面的双引号转义字符要转义回来
            String content = HtmlUtils.htmlUnescape(follow.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            Set<Map.Entry<String, Object>> entries = data.entrySet();
            for(Map.Entry<String, Object> entry : entries) {
                followVo.put(entry.getKey(), entry.getValue());
            }
            followVo.put("user", userService.findUserById((Integer) data.get("userId")));

            int followCount = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            followVo.put("count", followCount);

            int followUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            followVo.put("unread", followUnreadCount);

            model.addAttribute("followNotice", followVo);
        }

        //查询系统通知总未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        //查询私信未读数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        User user = hostHolder.getUser();

        //设置分页
        page.setLimit(5);
        page.setPath("/message/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null) {
            for(Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容
                Map<String, Object> data = JSONObject.parseObject(HtmlUtils.htmlUnescape(notice.getContent()), HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                Set<Map.Entry<String, Object>> entries = data.entrySet();
                for(Map.Entry<String, Object> entry : entries) {
                    map.put(entry.getKey(), entry.getValue());
                }
                //通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
