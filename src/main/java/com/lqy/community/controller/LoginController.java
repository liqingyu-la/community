package com.lqy.community.controller;

import com.lqy.community.entity.User;
import com.lqy.community.service.UserService;
import com.lqy.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @GetMapping(value = "/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @GetMapping(value = "/login")
    public String getLoginPage(){
        return "/site/login";
    }

    @PostMapping(value = "/register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封邮件，请尽快激活！");
            model.addAttribute("target","/index");//目标设置
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";//前面不能加‘/’？？？
        }
    }

    //        http://localhost:80808/community/activatoin/101/code
    @GetMapping(value = "/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId") Integer userId,
                             @PathVariable("code") String code){

        int result = userService.activation(userId, code);
        System.out.println(result);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用");
            model.addAttribute("target","/login");
        } else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已经激活过了");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码错误");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }





}
