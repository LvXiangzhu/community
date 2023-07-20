package com.dlut.community;

import com.dlut.community.dao.*;
import com.dlut.community.pojo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MappperTest {
    @Autowired
    UserMapper userMapper;
    @Autowired
    DiscussPostMapper discussPostMapper;
    @Autowired
    LoginTicketMapper loginTicketMapper;
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user1 = userMapper.selectById(1);
        System.out.println(user1);
//		User user2 = userMapper.selectByName("liubei");
//		System.out.println(user2);
//
//		User user3 = userMapper.selectByEmail("nowcoder101@sina.com");
//		System.out.println(user3);

    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("testname");
        user.setPassword("12345");
        user.setEmail("1234@163.com");
        user.setSalt("123");
        user.setHeaderUrl("http://www.nowcoder.com/100.png");
        user.setCreateTime(new Date());
        int i = userMapper.insertUser(user);
        System.out.println(i);
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateHeader(150, "http://www.newcoder.com/123.png");
        userMapper.updatePassword(150, "111");
        userMapper.updateStatus(150, 0);
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts() {
        int rows = discussPostMapper.selectDiscussPostRows(111);
        System.out.println(rows);
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(111, 0, 10, 0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setId(1);
        loginTicket.setUserId(101);
        loginTicket.setTicket("123");
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10)); //System.currentTimeMillis()单位：毫秒
        int row = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(row);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("123");
        System.out.println(loginTicket);
    }

    @Test
    public void testUpdateLoginTicket() {
        loginTicketMapper.updateStatus("123", 1);
    }

    @Test
    public void testUpdatePassword() {
        userMapper.updatePassword(150, "222");
    }

    @Test
    public void testInsertDiscussPost() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(150);
        discussPost.setTitle("测试数据");
        discussPost.setContent("大家好");
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        int i = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(i);
    }

    @Test
    public void testInsertComment() {
        Comment comment = new Comment();
        comment.setContent("text");
        comment.setCreateTime(new Date());
        comment.setUserId(151);
        comment.setEntityType(0);
        int i = commentMapper.insertComment(comment);
        System.out.println(i);
    }

    @Test
    public void testMessage() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        List<Message> list2 = messageMapper.selectLetters("111_112", 0, 5);
        for (Message message : list2) {
            System.out.println(message);
        }
        int c2 = messageMapper.selectLetterCount("111_112");
        System.out.println(c2);

        int c3 = messageMapper.selectLetterUnreadCount(111, "111_131");
        System.out.println(c3);
    }


}
