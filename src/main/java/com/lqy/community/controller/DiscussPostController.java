package com.lqy.community.controller;


import com.lqy.community.entity.Comment;
import com.lqy.community.entity.DiscussPost;
import com.lqy.community.entity.Page;
import com.lqy.community.entity.User;
import com.lqy.community.service.CommentService;
import com.lqy.community.service.DiscussPostService;
import com.lqy.community.service.UserService;
import com.lqy.community.util.CommunityConstant;
import com.lqy.community.util.CommunityUtil;
import com.lqy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Component
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

     @PostMapping(value = "/add")
     @ResponseBody
    public String addDiscussPost(String title, String content){
         User user = hostHolder.getUser();
         if (user == null){
             return CommunityUtil.getJSONString(403, "你还没登录");
         }
         DiscussPost discussPost = new DiscussPost();
//         discussPost.setId();
         discussPost.setUserId(user.getId());
         discussPost.setTitle(title);
         discussPost.setContent(content);
         discussPost.setCreateTime(new Date());
         discussPostService.addDiscussPost(discussPost);

//         报错情况，将来统一处理
         return CommunityUtil.getJSONString(0, "发布成功");
     }


//     帖子详情
    @GetMapping(value = "/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
         //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论VO（ViewObject）列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentVoList != null){
            for (Comment comment : commentList){
                //评论VO
                Map<String , Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyVoList != null){
                    for (Comment reply : replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target  = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

}
