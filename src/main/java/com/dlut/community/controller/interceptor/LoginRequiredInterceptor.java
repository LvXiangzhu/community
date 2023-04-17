package com.dlut.community.controller.interceptor;

import com.dlut.community.annotation.LoginRequired;
import com.dlut.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截的有可能是方法也有可能是静态资源，这里我们要拦截的是方法
        //所以用if判断表示拦截到的是方法
        //之后获取方法上的注解LoginRequired,我们要对它做拦截判断：
        //如果HostHolder能获取到用户，则放行；如果获取不到用户，则不放行
        if(handler instanceof HandlerMethod) {
            //把handler转换成HandlerMethod类型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取拦截到的方法
            Method method = handlerMethod.getMethod();
            //获取LoginRequired注解
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            if(annotation != null && hostHolder.getUser() == null) {
                //拒绝请求以后我们要做一个反馈强制重定向到登录界面
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
