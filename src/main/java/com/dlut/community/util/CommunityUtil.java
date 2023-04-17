package com.dlut.community.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
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

    /*
    * 功能：封装传入的json数据
    * 参数：code 编码
    *      msg 提示信息
    *      map 业务数据
    * */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        //传入的对象装到json
        json.put("code", code);
        json.put("msg", msg);
        if(map != null) {
            for(String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }
    //重载
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
