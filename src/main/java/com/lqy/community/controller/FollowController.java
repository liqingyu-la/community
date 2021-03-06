package com.lqy.community.controller;

import com.lqy.community.entity.DiscussPost;
import com.lqy.community.entity.Event;
import com.lqy.community.entity.Page;
import com.lqy.community.entity.User;
import com.lqy.community.event.EventProducer;
import com.lqy.community.service.FollowService;
import com.lqy.community.service.LikeService;
import com.lqy.community.service.UserService;
import com.lqy.community.util.CommunityConstant;
import com.lqy.community.util.CommunityUtil;
import com.lqy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") Integer userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null){
            for (Map<String, Object> map : followees){
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followees);
        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") Integer userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followers != null){
            for (Map<String, Object> map : followers){
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followers);
        return "/site/follower";
    }


//    收藏
    @GetMapping("/user/myCollect/{userId}")
    public String getCollection(@PathVariable("userId") Integer userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        int postCount = (int) followService.findFolloweeCount(userId, ENTITY_TYPE_POST);
        model.addAttribute("postCount", postCount);

        page.setLimit(5);
        page.setPath("/user/myCollect/" + userId);
        page.setRows(postCount);

        List<Map<String, Object>> collection = followService.findCollection(userId, page.getOffset(), page.getLimit());
        if (collection != null){
            for (Map<String, Object> map : collection){
                DiscussPost discussPost = (DiscussPost) map.get("discussPost");
                map.put("hasCollected", hasCollected(discussPost.getId()));

//                map.put("discussPost", discussPost);
                User creator = userService.findUserById(discussPost.getUserId());
                map.put("creator", creator);

                //获取点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);

//                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", collection);
        return "/site/my-collect";
    }

    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

    private boolean hasCollected(int discussPostId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
    }
}
