package com.dlut.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling //支持定时任务
@EnableAsync //让多线程注解生效
public class ThreadPoolConfig {
}
