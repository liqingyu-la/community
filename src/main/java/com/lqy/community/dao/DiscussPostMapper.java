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
}
