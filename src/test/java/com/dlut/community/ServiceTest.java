package com.dlut.community;

import com.dlut.community.Service.CommentService;
import com.dlut.community.Service.DiscussPostService;
import com.dlut.community.Service.LikeService;
import com.dlut.community.pojo.Comment;
import com.dlut.community.pojo.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ServiceTest {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Test
    public void testAddDiscussPost() {

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(150);
        discussPost.setTitle("赌博嫖娼");
        discussPost.setContent("大家好，有人赌博吗，嫖娼也可以，一起啊");
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPostService.addDiscussPost(discussPost);

        discussPost.setUserId(151);
        discussPost.setTitle("呵呵");
        discussPost.setContent("楼上傻逼");
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPostService.addDiscussPost(discussPost);

        discussPost.setUserId(149);
        discussPost.setTitle("老铁们点击一下我发的按钮");
        discussPost.setContent("<input type=\"button\">点击就送<\\input>");
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPostService.addDiscussPost(discussPost);
    }

    @Test
    public void testComment() {
        List<Comment> comments = commentService.findCommentsByEntity(1, 232, 1, 5);
        System.out.println(comments);
        int rows = commentService.findCommentCount(1, 232);
        System.out.println(rows);
    }

//    @Test
//    public void testLike() {
//        likeService.like(111, 1, 101);
//        long count = likeService.findEntityLikeCount(1, 101);
//        int status1 = likeService.findEntityLikeStatus(111, 1, 101);
//        System.out.println(count+":"+status1);
//    }
}
