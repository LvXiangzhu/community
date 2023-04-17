package com.dlut.community.Service;

import com.dlut.community.dao.DiscussPostMapper;
import com.dlut.community.pojo.DiscussPost;
import com.dlut.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPost(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost) {
        //discussPost可能为空，所以需要判空抛异常
        if(discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //类似<script></script>的标签也需要处理，防止破坏网页
        //转义html: HtmlUtils.htmlEscape()可以直接把标签去掉
//        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
//        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        String title = discussPost.getTitle();
        String content = discussPost.getContent();

        //过滤敏感词工具不要创建新对象！直接用Spring容器注入进来
//        SensitiveFilter sensitiveFilter = new SensitiveFilter();
        discussPost.setTitle(sensitiveFilter.filter(title));
        discussPost.setContent(sensitiveFilter.filter(content));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }
}
