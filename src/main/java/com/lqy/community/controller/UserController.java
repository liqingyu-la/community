package com.lqy.community.controller;


import ch.qos.logback.classic.spi.EventArgUtil;
import com.lqy.community.annotation.LoginRequired;
import com.lqy.community.entity.Comment;
import com.lqy.community.entity.DiscussPost;
import com.lqy.community.entity.Page;
import com.lqy.community.entity.User;
import com.lqy.community.service.*;
import com.lqy.community.util.CommunityConstant;
import com.lqy.community.util.CommunityUtil;
import com.lqy.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.network.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;//项目访问路径

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @GetMapping(value = "/setting")
    public String getSettingPage(){
        return "/site/setting";
    }


//  上传头像
    @LoginRequired
    @PostMapping(value = "/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
//        截取后缀
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
//        随机生成文件名
        fileName = CommunityUtil.generateUUID() + suffix;
//        确定文件存放路径
        File dest = new File(uploadPath + "/" +fileName);
        try {
            headerImage.transferTo(dest);//写入目标文件
        } catch (IOException e) {
            logger.error("上传文件失败，服务器发生异常！" + e);
        }
//        更新当前用户的头像路径（web访问路径）
//        http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }


//    获取头像
    @GetMapping(value = "/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
//        服务器存放路径
        fileName = uploadPath + "/" + fileName;
//      文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        响应图片
        response.setContentType("image/" + suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ){
                    byte[] buffer = new byte[1024];
                    int b = 0;
                    while ( (b = fis.read(buffer)) != -1) {
                        os.write(buffer,0,b);
                    }
        } catch (IOException e) {
            logger.error("读取头像失败： " + e.getMessage());
        }

    }

//    修改密码
    @PostMapping(value = "/updatePassword")
    public String changePassword(String oldPassword,String newPassword, Model model) {

        System.out.println("oldPassword   " + oldPassword);

        User user = hostHolder.getUser();
        Map<String, Object> map = userService.changePassword(user,oldPassword, newPassword);
        if(map == null || map.isEmpty()){
            return "redirect:/index";
        }else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }
    //个人主页
    @GetMapping(value = "/profile/{userId}")
    public String getProfilePage(@PathVariable(value = "userId") int userId, Model model){

        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }

        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";

    }

    //我的帖子
    @GetMapping(value = "/myPosts/{userId}")
    public String getMyPosts(@PathVariable(value = "userId") int userId, Model model, Page page){

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        int postCount = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("postCount", postCount);

        page.setRows(postCount);
        page.setLimit(5);
        page.setPath("/user/myPosts/" + userId);

        List<DiscussPost> list = discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost post : list){
                Map<String,Object> map = new HashMap<>();
                map.put("post", post);

                //获取点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);

        return "/site/my-post";
    }

    //我的评论回复
    @GetMapping(value = "/myReplies/{userId}")
    public String getMyReplies(@PathVariable(value = "userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        int commentCount = commentService.findCommentCountByUserId(userId);
        model.addAttribute("commentCount", commentCount);

        page.setRows(commentCount);
        page.setLimit(5);
        page.setPath("/user/myReplies/" + userId);

        List<Comment> list = commentService.findCommentsByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> comments = new ArrayList<>();
        if (list != null){
            for (Comment comment : list){
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                String discussPostTitle = discussPostService.findDiscussPostById(comment.getEntityId()).getTitle();
                map.put("discussPostTitle", discussPostTitle);


                comments.add(map);
            }

            model.addAttribute("comments", comments);
        }
        return "site/my-reply";
    }


}
