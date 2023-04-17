package com.dlut.community.controller.advice;

import com.dlut.community.pojo.Comment;
import com.dlut.community.util.CommunityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;

//防止ControllerAdvice扫描所有的Bean，范围太大了，所以括号里进行限制，只扫描带有Controller注解的Bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(Exception.class);


    /*
    * 统一处理异常
    * */
    @ExceptionHandler({Exception.class}) //括号里写处理哪些异常，这里处理所有异常
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常："+e.getMessage());
        for(StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //可能是普通请求，返回页面，也可能是异步请求，返回json
        //什么类型的请求从request的Header里取
        String header = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(header)) {
            //XMLHttpRequest表示json异步请求
            response.setContentType("applicaion/plain;charset=utf-8");//一个普通格式的json字符串
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        } else{
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }

}
