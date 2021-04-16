package com.lqy.community.service;

import com.lqy.community.dao.DiscussPostMapper;
import com.lqy.community.dao.LoginTicketMapper;
import com.lqy.community.dao.UserMapper;
import com.lqy.community.entity.DiscussPost;
import com.lqy.community.entity.LoginTicket;
import com.lqy.community.entity.User;
import com.lqy.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Repository
public class UserService implements CommunityConstant {

/*    @Autowired
    private LoginTicketMapper loginTicketMapper;*/

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;//模板引擎

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Value("${community.path.domain}")
    private String domain;//域名

    @Value("${server.servlet.context-path}")
    private String contextPath;//项目名

    public User findUserById(int userId){
//        return userMapper.selectById(userId);
        User user = getCache(userId);
        if (user == null){
            user = initCache(userId);
        }
        return user;
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
//        空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

//        验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg", "账号已存在");
            return map;
        }
//        验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg", "邮箱已存在");
            return map;
        }


        //    注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

//        激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
//        http://localhost:80808/community/activatoin/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

//    激活业务
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

//    登录业务
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

//        空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user  = userMapper.selectByName(username);
//        验证账号
        if (user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
//        验证状态
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

//        验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

//        生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(expiredSeconds * 1000 + System.currentTimeMillis()));
        loginTicket.setTicket(CommunityUtil.generateUUID());
//        loginTicketMapper.insertLoginTicketMapper(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
//        System.out.println(redisKey + "   " + loginTicket);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

//    退出
    public void logout(String ticket){
//            loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket  = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

//    查询凭证
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

//    更新头像
    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

//    修改密码
    public Map<String, Object> changePassword(User user, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        // 验证密码
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "密码不正确!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "密码不能为空!");
            return map;
        }
        int id=user.getId();
        newPassword=CommunityUtil.md5(newPassword + user.getSalt());
        if(oldPassword.equals(newPassword)){
            map.put("newPasswordMsg", "旧密码与新密码一致!");
            return map;
        }
        userMapper.updatePassword(id,newPassword);
        clearCache(user.getId());
        return map;
    }

//    根据用户名查找用户
    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

//  1.	优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
//2.	取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
//3.	数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


}
