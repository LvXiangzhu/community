package com.dlut.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    //切点
    @Pointcut("execution(* com.dlut.community.Service.*.*(..))")
    public void pointcut() {}

    //通知
    @Before("pointcut()")
    public void before() {
        System.out.println("Before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("After");
    }

    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("AfterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("AfterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Around before");
        Object obj = joinPoint.proceed();
        System.out.println("Around after");
        return obj;
    }
}
