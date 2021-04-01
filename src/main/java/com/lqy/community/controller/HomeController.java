package com.lqy.community.controller;

import com.lqy.community.dao.DiscussPostMapper;
import com.lqy.community.entity.DiscussPost;
import com.lqy.community.entity.Page;
import com.lqy.community.entity.User;
import com.lqy.community.service.DiscussPostService;
import com.lqy.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    @GetMapping(value = "/index")
    public String getIndexPage(Model model, Page page){/*Page用来接受页面传过来的分页信息*/
//        方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入到Model
//        所以，在thymeleaf中可以直接访问Page
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost post : list){
                Map<String,Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @GetMapping(value = "/error")
    public String  getErrorPage(){
        return "/error/500";
    }
}
