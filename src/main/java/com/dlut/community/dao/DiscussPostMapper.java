package com.dlut.community.dao;

import com.dlut.community.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /*
    * 动态查询。
    * userId = 0时，查询所有用户帖子
    * userId ！= 0时，查询当前用户帖子
    * 分页
    * offset:每页起始行行号
    * limit:每页最多显示数据
    * orderMode:排序方式，按热度还是时间
    * */
    List<DiscussPost> selectDiscussPost(int userId, int offset, int limit, int orderMode);

    /*
    * 查询帖子数量
    * 参数：用户Id。Id=0时查询全部
    * 注：当参数为动态查询参数且仅有一个参数时，必须用@Param起别名，否则报错
    * */
    int selectDiscussPostRows(@Param("userId") int userId);

    /*
    * 增加帖子
    * */
    int insertDiscussPost(DiscussPost discussPost);

    /*
    * 查询帖子
    * */
    DiscussPost selectDiscussPostById(int id);

    /*
    * 更新评论数量（comment_count）
    * */
    int updateCommentCount(int id, int commentCount);

    /*
    * 更改帖子类型
    * */
    int updateType(int id, int type);

    /*
    * 更改帖子状态
    * */
    int updateStatus(int id, int status);

    /*
     * 更改帖子分数
     * */
    int updateScore(int id, double score);
}
