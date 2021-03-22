package com.lqy.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
//    生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");

    }


//        MD5加密
//        hello -> asda474fgsdc54
//        hello + 3e4b8(salt字段 随机字符串) -> gasgcjvcu4874syv4654cbs4654gcvaks(加密结果)
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
