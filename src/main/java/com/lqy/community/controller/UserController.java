package com.lqy.community.controller;


import com.lqy.community.annotation.LoginRequired;
import com.lqy.community.entity.User;
import com.lqy.community.service.UserService;
import com.lqy.community.util.CommunityUtil;
import com.lqy.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
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

@Controller
@RequestMapping("/user")
public class UserController {

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

/*//    修改密码
    @PutMapping(value = "/updatePassword")
    public String updatePassword(Model model,String password){

        User user = hostHolder.getUser();
        userService.updatePassword(user.getId(), password);
    }*/



}