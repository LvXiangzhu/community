package com.dlut.community.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {
    /*
    * 方法功能：从Cookie中取值
    * 参数：request对象，cookie名字
    * 返回值：cookie的值
    * */
    public static String getValue(HttpServletRequest request, String name) {
        if(request == null || name == null) {
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
