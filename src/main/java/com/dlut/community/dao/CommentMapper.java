package com.dlut.community.dao;

import com.dlut.community.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /*
    * 根据实体类型和实体Id分页查询评论
    * 参数：实体类型，实体Id，起始行，每页显示行数
    * */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /*
     * 查询数据条目数
     * 参数：实体类型（评论还是回复），
     * */
    int selectCountByEntity(int entityType, int entityId);

    /*
    * 增加评论
    * */
    int insertComment(Comment comment);

    Comment selectCommentsById(int id);
}
