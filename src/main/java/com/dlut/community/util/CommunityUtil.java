package com.dlut.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串（激活码，上传头像时随机命名时都会用到）
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //MD5加密（给密码加密，防止泄露）
    // MD5只能加密不能解密 hello -> abc123def
    // 防止密码过于简单，需要加随机字符串 hello + 2hdjb -> abc123defabc
    public static String md5(String key) { //传进来的key是加salt之后的字符串
        if(StringUtils.isBlank(key)) { //判空
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes()); //加密
    }
}
