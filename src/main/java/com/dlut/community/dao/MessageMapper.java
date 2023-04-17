package com.dlut.community.dao;

import com.dlut.community.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量 有查询所有未读私信和某个对话未读私信两种方法，
    // 如果有以String conversationId参数，就是查某个对话未读私信，否则查询所有未读私信
    int selectLetterUnreadCount(int userId, String conversationId);

    //增加一个消息
    int insertMessage(Message message);

    //更改消息状态
    int updateStatus(List<Integer> ids, int status);

    //查询最新的一条系统消息
    Message selectLatestNotice(int userId, String topic);

    //查询系统消息总数
    int selectNoticeCount(int userId, String topic);

    //查询未读消息数量
    int selectNoticeUnreadCount(int userId, String topic);

    //查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
