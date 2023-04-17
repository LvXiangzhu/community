package com.dlut.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //声明注解写在方法上
@Retention(RetentionPolicy.RUNTIME) //声明注解运行时有效
public @interface LoginRequired {
}
