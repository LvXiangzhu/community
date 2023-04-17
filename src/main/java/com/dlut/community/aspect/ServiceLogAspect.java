package com.dlut.community.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //切点
    @Pointcut("execution(* com.dlut.community.Service.*.*(..))")
    public void pointcut() {}

    //在方法执行前记录日志
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //用户[1,2,3,4],在[xxx]时间,访问了[com.dlut.community.service.xxx()]

        //通过RequestContextHolder得到request请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(attributes == null) {
            //非常规页面Controller调用，没有request和response
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        //通过request得到请求的ip地址
        String ip = request.getRemoteHost();
        //访问时间：当前时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //访问哪个类哪个方法：需要参数JoinPoint
        //类名+方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s]",ip, now, target));
    }
}
