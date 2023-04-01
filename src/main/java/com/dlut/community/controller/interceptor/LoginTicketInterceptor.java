package com.dlut.community.controller.interceptor;

import com.dlut.community.Service.UserService;
import com.dlut.community.pojo.LoginTicket;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CookieUtil;
import com.dlut.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket != null) {
            //有ticket表示已经登录
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //根据凭证查询user
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有user
                //防止多线程中多个对象冲突，在多线程中用ThreadLocal隔离存储多个对象
                hostHolder.setUser(user);
            }

        }
        return true;
    }

    /*
    * 需要在模板之前得到user
    * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    /*
    * 在模板之后清理user
    * */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
