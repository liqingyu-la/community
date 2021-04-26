package com.lqy.community.dao;

import com.lqy.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DiscussPostMapper {

//    offset起始行，limit每页最多显示多少行
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

//    @Param注解用来给参数取别名
//    如果只有一个参数且在<if>里使用，必须加别名
//    查询一共有多少帖子
    int selectDiscussPostRows(@Param("userId") int userId);

//    增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);


    //更新科目
    int updateDiscussPostSection(int id, int section);

    //查找科目
    List<DiscussPost> selectSectionDiscussPosts(int section, int offset, int limit);

    //查找该科目数量
    int selectSectionDiscussPostsRows(int section);




}
